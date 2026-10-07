#!/usr/bin/env bash
# Compose's original files remain unchanged; one workflow-owned override pins images and network identity.

release_compose_base() (
  cd "$DEPLOY_DIR" || exit
  docker compose --project-name "$PROJECT_NAME" \
    --env-file "$COMPOSE_BASE_ENV" --env-file .env.docker-compose-profile-mid-level \
    --file docker-compose.yaml "$@"
)

compose_override_path() {
  printf '%s/release-test-%s.json' "$RUNNER_TEMP" "$PROJECT_NAME"
}

write_compose_override() {
  local network_name override_file registry_hostname
  network_name=${COMPOSE_NETWORK_NAME:-cardano-rosetta-java-${NETWORK}}
  override_file=$(compose_override_path) || return
  registry_hostname=${TOKEN_REGISTRY_HOSTNAME:-}

  jq -n \
    --arg api "$API_IMAGE" \
    --arg indexer "$INDEXER_IMAGE" \
    --arg node "$CARDANO_NODE_IMAGE" \
    --arg postgres "$POSTGRES_IMAGE" \
    --arg mithril "$MITHRIL_IMAGE" \
    --arg network "$network_name" \
    --arg registry_hostname "$registry_hostname" '
      {
        services: {
          api: {image: $api},
          "yaci-indexer": {image: $indexer},
          db: {image: $postgres},
          "cardano-node": {image: $node},
          "cardano-submit-api": {image: $node},
          "cardano-sync-waiter": {image: $node},
          mithril: {image: $mithril}
        },
        networks: {default: {name: $network}}
      }
      | if $registry_hostname == "" then .
        else .services.api.extra_hosts = [($registry_hostname + ":host-gateway")]
        end
    ' > "$override_file" || return
  echo "Wrote explicit Compose image and network configuration to $override_file."
}

release_compose() (
  local override_file
  override_file=$(compose_override_path) || exit
  if [[ ! -s "$override_file" ]]; then
    echo "Compose override is missing or empty: $override_file" >&2
    exit 1
  fi
  cd "$DEPLOY_DIR" || exit
  docker compose --project-name "$PROJECT_NAME" \
    --env-file "$COMPOSE_BASE_ENV" --env-file .env.docker-compose-profile-mid-level \
    --file docker-compose.yaml --file "$override_file" "$@"
)

stop_compose_release() {
  write_compose_override || return
  release_compose down --remove-orphans
}

verify_compose_database_access() (
  # Resolve exactly the application's Compose settings, including environment overrides.
  # Keep the password out of command arguments and shell traces.
  set +x
  local config db_container
  config=$(release_compose_base config --format json | jq -ce '
    .services.api.environment | {DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_SECRET}
    | select(all(.[]; type == "string" and length > 0))
  ') || { echo "Could not resolve candidate Compose database settings." >&2; exit 1; }

  db_container=$(docker ps \
    --filter "label=com.docker.compose.project=${PROJECT_NAME}" \
    --filter 'label=com.docker.compose.service=db' --format '{{.ID}}')
  [[ -n "$db_container" && "$db_container" != *$'\n'* ]] || {
    echo "Expected exactly one running database container for $PROJECT_NAME." >&2; exit 1;
  }

  local PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD
  PGHOST=$(jq -r '.DB_HOST' <<< "$config")
  PGPORT=$(jq -r '.DB_PORT' <<< "$config")
  PGDATABASE=$(jq -r '.DB_NAME' <<< "$config")
  PGUSER=$(jq -r '.DB_USER' <<< "$config")
  PGPASSWORD=$(jq -r '.DB_SECRET' <<< "$config")
  export PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD
  # Use the application host/port, not the local Unix socket's trust authentication.
  if ! docker exec --env PGHOST --env PGPORT --env PGDATABASE --env PGUSER \
      --env PGPASSWORD --env PGCONNECT_TIMEOUT=10 "$db_container" \
      psql --no-psqlrc --no-password --set ON_ERROR_STOP=1 --command 'SELECT 1;'; then
    echo "Candidate Compose database authentication failed; deployment must not proceed." >&2
    exit 1
  fi
)

prepare_compose_images() {
  local image
  for image in "$API_IMAGE" "$INDEXER_IMAGE" "$CARDANO_NODE_IMAGE" "$POSTGRES_IMAGE" "$MITHRIL_IMAGE"; do
    docker pull "$image"
  done
  write_compose_override || return
}

compose_candidate_services() {
  printf '%s\n' "api|$API_IMAGE" "yaci-indexer|$INDEXER_IMAGE" "db|$POSTGRES_IMAGE" \
    "cardano-node|$CARDANO_NODE_IMAGE" "cardano-submit-api|$CARDANO_NODE_IMAGE" \
    "mithril|$MITHRIL_IMAGE" "cardano-sync-waiter|$CARDANO_NODE_IMAGE"
}

verify_compose_config() {
  local config service image configured_image
  config=$(release_compose config --format json)
  while IFS='|' read -r service image; do
    configured_image=$(jq -r --arg service "$service" '.services[$service].image' <<< "$config")
    if [[ "$configured_image" != "$image" ]]; then
      echo "Compose $service renders $configured_image, expected $image." >&2
      return 1
    fi
  done < <(compose_candidate_services)
  if ! jq -e '
      .services.api.environment.REMOVE_SPENT_UTXOS == "false" and
      .services["yaci-indexer"].environment.REMOVE_SPENT_UTXOS == "false"
    ' <<< "$config"; then
    echo "Compose does not render the required full-history configuration." >&2
    return 1
  fi
}

verify_compose_container() {
  local service=$1 expected_image=$2 reference_policy=${3:-tag-or-digest}
  local configured_image running_id expected_id containers reference_matches=false
  local -a all=()
  case "$service" in mithril|cardano-sync-waiter) all=(--all) ;; esac
  containers=$(docker ps "${all[@]}" \
    --filter "label=com.docker.compose.project=${PROJECT_NAME}" \
    --filter "label=com.docker.compose.service=${service}" --format '{{.ID}}')
  [[ -n "$containers" && "$containers" != *$'\n'* ]] || {
    echo "Expected exactly one $service container for $PROJECT_NAME." >&2; exit 1;
  }
  if [[ "$service" == api || "$service" == yaci-indexer ]]; then
    verify_compose_history "$containers"
  fi
  configured_image=$(docker inspect "$containers" --format '{{.Config.Image}}')
  running_id=$(docker inspect "$containers" --format '{{.Image}}')
  expected_id=$(docker image inspect "$expected_image" --format '{{.Id}}')
  case "$reference_policy" in
    immutable)
      [[ "$configured_image" == "$expected_image" ]] && reference_matches=true
      ;;
    tag-or-digest)
      if [[ "$configured_image" == "$expected_image" ||
            "$configured_image" == "${expected_image%@*}" ]]; then
        reference_matches=true
      fi
      ;;
    *) echo "Unknown Compose image-reference policy: $reference_policy" >&2; exit 1 ;;
  esac
  if [[ "$reference_matches" != true || "$running_id" != "$expected_id" ]]; then
    echo "Compose $service image mismatch: configured=$configured_image running=$running_id expected=$expected_image ($expected_id)." >&2
    exit 1
  fi
}

verify_compose_history() {
  local history
  history=$(docker inspect "$1" | jq -r \
    '.[0].Config.Env[] | select(startswith("REMOVE_SPENT_UTXOS=")) | sub("^REMOVE_SPENT_UTXOS="; "")')
  [[ "$history" == false ]] || { echo "Compose $1 is not in full-history mode." >&2; exit 1; }
}

verify_compose_candidate() {
  local service image
  while IFS='|' read -r service image; do
    verify_compose_container "$service" "$image" immutable
  done < <(compose_candidate_services)
}

verify_and_prefetch_compose_target() {
  prepare_compose_images
  verify_compose_config
}

wait_for_compose_target_live() {
  source "$RELEASE_SCRIPTS/release_test_runtime.sh"
  wait_for_release_live http://127.0.0.1:8082 mainnet "${PRERELEASE_TAG%-pre-release}"
  verify_compose_candidate
  wait_for_mainnet_token_metadata http://127.0.0.1:8082
}

reset_current_compose_release_data() {
  local db_path=$DB_PATH node_path=$CARDANO_NODE_DIR cleanup_root=$DATA_ROOT path
  local minimum_free_gib=${COMPOSE_MIN_FREE_GIB:-}
  source "$RELEASE_SCRIPTS/release_test_runtime.sh" || return
  require_release_host || return
  if [[ "$cleanup_root" == / || "$db_path" != "$cleanup_root/sql_data" ||
        "$node_path" != "$cleanup_root/node_data" ]]; then
    echo "Refusing to delete data outside the configured release root." >&2
    return 1
  fi
  for path in "$cleanup_root" "$db_path" "$node_path"; do
    if [[ ! -d "$path" || -L "$path" || "$(realpath -e "$path")" != "$path" ]]; then
      echo "Expected a real, canonical data directory: $path" >&2
      return 1
    fi
  done

  stop_compose_release || return
  sudo find "$db_path" "$node_path" -xdev -mindepth 1 -delete || return
  if [[ -n "$minimum_free_gib" ]]; then
    require_release_free_space "$cleanup_root" "$minimum_free_gib" || return
  fi
  echo "Compose release data removed."
}
