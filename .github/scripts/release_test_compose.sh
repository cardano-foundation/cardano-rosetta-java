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
  local network_name override_file
  network_name=${COMPOSE_NETWORK_NAME:-cardano-rosetta-java-${NETWORK}}
  override_file=$(compose_override_path) || return

  jq -n \
    --arg api "$API_IMAGE" \
    --arg indexer "$INDEXER_IMAGE" \
    --arg node "$CARDANO_NODE_IMAGE" \
    --arg postgres "$POSTGRES_IMAGE" \
    --arg mithril "$MITHRIL_IMAGE" \
    --arg network "$network_name" '
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

# Before stopping: require ownership. After stopping: no container may retain a
# path, its parent, or its descendants. Docker errors must never look like no mounts.
verify_compose_mounts() {
  local policy=$1 containers container mounts mount data_path project service
  containers=$(docker ps --all --quiet) || return
  for container in $containers; do
    mounts=$(docker inspect "$container" \
      --format '{{range .Mounts}}{{println .Source}}{{end}}') || return
    while IFS= read -r mount; do
      [[ -n "$mount" ]] || continue
      for data_path in "$DB_PATH" "$CARDANO_NODE_DIR"; do
        if [[ "$mount" != "$data_path" && "$data_path" != "$mount"/* && "$mount" != "$data_path"/* ]]; then
          continue
        fi
        [[ "$policy" != unmounted ]] || {
          echo "Refusing deletion: $container still references $data_path via $mount." >&2; exit 1;
        }
        project=$(docker inspect "$container" \
          --format '{{index .Config.Labels "com.docker.compose.project"}}') || return
        [[ "$project" == "$PROJECT_NAME" ]] || {
          echo "Data path $mount belongs to foreign project $project." >&2; exit 1;
        }
        if [[ "$policy" == services ]]; then
          service=$(docker inspect "$container" \
            --format '{{index .Config.Labels "com.docker.compose.service"}}') || return
          if [[ "$data_path" == "$DB_PATH" ]]; then
            [[ "$service" == db ]] || { echo "Unexpected database service $service." >&2; exit 1; }
          else
            case "$service" in
              api|cardano-node|cardano-submit-api|cardano-sync-waiter|mithril|yaci-indexer) ;;
              *) echo "Unexpected node-data service $service." >&2; exit 1 ;;
            esac
          fi
        fi
      done
    done <<< "$mounts"
  done
}

reset_current_compose_release_data() {
  local db_path=$DB_PATH node_path=$CARDANO_NODE_DIR cleanup_root=$DATA_ROOT
  local minimum_free_gib=${COMPOSE_MIN_FREE_GIB:-}
  local root_device host_mounts data_path expected_identity mount free_bytes free_gib minimum_free_bytes
  local db_identity node_identity

  source "$RELEASE_SCRIPTS/release_test_runtime.sh" || return
  require_release_host || return
  if [[ "$db_path" != "$cleanup_root/sql_data" || "$node_path" != "$cleanup_root/node_data" ||
        "$cleanup_root" == "/" ]]; then
    echo "Refusing to delete data outside the configured release root." >&2
    return 1
  fi
  if [[ "$(realpath -e "$cleanup_root")" != "$cleanup_root" || -L "$cleanup_root" ]]; then
    echo "Refusing cleanup through a non-canonical root: $cleanup_root" >&2
    return 1
  fi
  if [[ ! -d "$db_path" || -L "$db_path" || ! -d "$node_path" || -L "$node_path" ]]; then
    echo "Compose data paths must exist as real directories before cleanup." >&2
    return 1
  fi

  db_identity=$(stat -c '%d:%i' "$db_path") || return
  node_identity=$(stat -c '%d:%i' "$node_path") || return
  verify_compose_mounts services || return
  if [[ -n "$minimum_free_gib" ]]; then
    require_release_cleanup_space \
      "$cleanup_root" "$minimum_free_gib" "$db_path" "$node_path" || return
  fi
  release_compose down --remove-orphans || return
  verify_compose_mounts unmounted || return

  root_device=$(stat -c '%d' "$cleanup_root") || return
  host_mounts=$(
    findmnt --json --output TARGET |
      jq -er '.filesystems[] | recurse(.children[]?) | .target'
  ) || return
  for data_path in "$db_path" "$node_path"; do
    if [[ "$data_path" == "$db_path" ]]; then
      expected_identity=$db_identity
    else
      expected_identity=$node_identity
    fi
    if [[ "$(realpath -e "$data_path")" != "$data_path" ]]; then
      echo "Refusing cleanup of a non-canonical data path: $data_path" >&2
      return 1
    fi
    if [[ "$(stat -c '%d' "$data_path")" != "$root_device" ]]; then
      echo "Refusing cleanup across a different filesystem: $data_path" >&2
      return 1
    fi
    if [[ "$(stat -c '%d:%i' "$data_path")" != "$expected_identity" ]]; then
      echo "Data directory identity changed immediately before cleanup: $data_path" >&2
      return 1
    fi
    while IFS= read -r mount; do
      if [[ "$mount" == "$data_path" || "$mount" == "$data_path"/* ]]; then
        echo "Refusing cleanup across host mount $mount under $data_path." >&2
        return 1
      fi
    done <<< "$host_mounts"
  done

  cd "$cleanup_root" || return
  sudo find ./sql_data ./node_data -xdev -mindepth 1 -delete || return
  if [[ -z "$minimum_free_gib" ]]; then
    echo "Compose release data removed."
    return 0
  fi

  free_bytes=$(df --output=avail -B1 "$cleanup_root" |
    awk 'NR == 2 {print $1}') || return
  [[ "$free_bytes" =~ ^[0-9]+$ ]] || {
    echo "Could not measure free space after Compose cleanup." >&2
    return 1
  }
  free_gib=$((free_bytes / 1024 / 1024 / 1024))
  minimum_free_bytes=$((minimum_free_gib * 1024 * 1024 * 1024))
  if (( free_bytes < minimum_free_bytes )); then
    echo "Compose reset left $free_bytes bytes free (${free_gib}GiB); at least ${minimum_free_gib}GiB is required before installation." >&2
    return 1
  fi
  echo "Compose release data removed with ${free_gib}GiB available."
}
