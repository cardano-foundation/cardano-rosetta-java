#!/usr/bin/env bash
# Cluster/storage identity and the supported Helm deployment contract.

verify_release_cluster() {
  local uid
  uid=$(kubectl get namespace kube-system -o json | jq -r '.metadata.uid')
  [[ "$uid" == "$EXPECTED_KUBE_SYSTEM_UID" ]] || {
    echo "Unexpected K8s cluster identity: $uid" >&2; exit 1;
  }
}

verify_release_database_secret() {
  local secret_json
  secret_json=$(kubectl get secret "$DB_SECRET_NAME" --namespace "$NAMESPACE" --output json)
  if ! jq -e --arg release "$HELM_RELEASE" '
    (.data["db-secret"] | type == "string" and length > 0) and
    .metadata.labels["app.kubernetes.io/instance"] == $release and
    .metadata.labels["app.kubernetes.io/managed-by"] == "Helm" and
    .metadata.annotations["helm.sh/resource-policy"] == "keep" and
    .metadata.annotations["meta.helm.sh/release-name"] == $release and
    .metadata.annotations["meta.helm.sh/release-namespace"] == .metadata.namespace and
    ((.metadata.ownerReferences // []) | length == 0)
  ' <<< "$secret_json" >/dev/null; then
    echo "$DB_SECRET_NAME does not satisfy the preserved release-secret contract." >&2
    return 1
  fi
}

# Emits PV name, PV UID and canonical data path as TSV. Call through an assignment,
# not a process substitution: API/filesystem failures must abort the caller.
verify_release_volume() (
  set -euo pipefail
  local pvc=$1 expected_uid=$2 pvc_json pv_json pv pv_uid data_path root_device path_device
  pvc_json=$(kubectl get pvc "$pvc" --namespace "$NAMESPACE" -o json) || exit
  pv=$(jq -er --arg release "$HELM_RELEASE" --arg uid "$expected_uid" '
    if .metadata.labels["app.kubernetes.io/instance"] == $release and
       .metadata.uid == $uid and
       .spec.storageClassName == "local-path" and .status.phase == "Bound"
    then .spec.volumeName else error("Unexpected release PVC contract") end
  ' <<< "$pvc_json") || exit
  pv_json=$(kubectl get pv "$pv" -o json) || exit
  pv_uid=$(jq -er '.metadata.uid' <<< "$pv_json") || exit
  data_path=$(jq -er --arg pvc "$pvc" --arg uid "$expected_uid" --arg namespace "$NAMESPACE" '
    if .spec.claimRef.uid == $uid and .spec.claimRef.name == $pvc and
       .spec.claimRef.namespace == $namespace and .spec.persistentVolumeReclaimPolicy == "Delete"
    then .spec.local.path else error("Unexpected release PV binding or reclaim policy") end
  ' <<< "$pv_json") || exit
  root_device=$(stat -c '%d' "$K8S_STORAGE_ROOT") || exit
  path_device=$(sudo stat -c '%d' "$data_path") || exit
  if [[ "$data_path" != "$K8S_STORAGE_ROOT"/* ]] ||
     ! sudo test -d "$data_path" || sudo test -L "$data_path" ||
     [[ "$(sudo realpath -e "$data_path")" != "$data_path" ]] ||
     [[ "$path_device" != "$root_device" ]]; then
    echo "Unsafe local-path storage target for $pvc: $data_path" >&2
    exit 1
  fi
  printf '%s\t%s\t%s\n' "$pv" "$pv_uid" "$data_path"
)

verify_release_unbound_pvc() {
  local pvc_json=$1 expected_uid=$2 validated
  validated=$(jq -er --arg release "$HELM_RELEASE" --arg uid "$expected_uid" '
    if .metadata.labels["app.kubernetes.io/instance"] == $release and
       .metadata.uid == $uid and
       .spec.storageClassName == "local-path" and
       ((.spec.volumeName // "") == "") and
       .status.phase == "Pending"
    then true else error("Unexpected unbound release PVC contract") end
  ' <<< "$pvc_json") || return
  [[ "$validated" == true ]]
}

# One snapshot per workload in the current step, never cached across phases.
# Populates workload_json/pods_json; container verification selects pod_json.
load_release_workload() {
  local kind=$1 name=$2
  kubectl rollout status "$kind/$HELM_RELEASE-$name" --namespace "$NAMESPACE" --timeout=10m
  workload_json=$(kubectl get "$kind" "$HELM_RELEASE-$name" --namespace "$NAMESPACE" -o json)
  pods_json=$(kubectl get pods --namespace "$NAMESPACE" \
    --selector "app=${HELM_RELEASE}-${name},component=${name}" --output json)
}

verify_release_container() {
  local container=$1 expected_image=$2 image
  image=$(jq -r --arg container "$container" \
    '.spec.template.spec.containers[] | select(.name == $container) | .image' <<< "$workload_json")
  [[ "$image" == "$expected_image" ]] || {
    echo "K8s $container template does not use $expected_image." >&2; exit 1;
  }
  pod_json=$(jq -cer --arg container "$container" --arg image "$expected_image" \
    --arg digest "${expected_image##*@}" '
      [.items[] | select(
        any(.spec.containers[]; .name == $container and .image == $image) and
        any(.status.containerStatuses[]?; .name == $container and .ready == true and (.imageID | endswith("@" + $digest)))
      )] | if length == 1 then .[0] else error("Expected one ready pod, found \(length)") end
    ' <<< "$pods_json")
}

verify_release_init_container() {
  local container=$1 expected_image=$2 image
  image=$(jq -r --arg container "$container" \
    '.spec.template.spec.initContainers[] | select(.name == $container) | .image' <<< "$workload_json")
  if [[ "$image" != "$expected_image" ]] || ! jq -e \
    --arg container "$container" --arg image "$expected_image" --arg digest "${expected_image##*@}" '
      [.items[] | select(
        any(.spec.initContainers[]; .name == $container and .image == $image) and
        any(.status.initContainerStatuses[]?; .name == $container and
          .state.terminated.exitCode == 0 and (.imageID | endswith("@" + $digest)))
      )] | length == 1
    ' <<< "$pods_json"; then
    echo "K8s $container did not complete from $expected_image." >&2
    exit 1
  fi
}

verify_release_deployment() {
  local deployment=$1 expected_image=$2 history init_container
  local workload_json pods_json pod_json
  shift 2
  load_release_workload deployment "$deployment"
  history=$(jq -r --arg container "$deployment" \
    '.spec.template.spec.containers[] | select(.name == $container) | .env[] |
     select(.name == "REMOVE_SPENT_UTXOS") | .value' <<< "$workload_json")
  [[ "$history" == false ]] || { echo "K8s $deployment is not full-history." >&2; exit 1; }

  verify_release_container "$deployment" "$expected_image"
  # Deployment init evidence must belong to that same ready, full-history pod.
  pods_json=$(jq -nc --argjson pod "$pod_json" '{items: [$pod]}')
  for init_container in "$@"; do
    verify_release_init_container "$init_container" "$CARDANO_NODE_IMAGE"
  done
}

release_helm() {
  local postgres_version_ref=${POSTGRES_IMAGE#cardanofoundation/cardano-rosetta-java-postgres:}
  local chart storage_values
  local -a storage_args=()
  chart=$(mktemp -d "$RUNNER_TEMP/release-chart.XXXXXX")
  cp -a "$DEPLOY_DIR/helm/cardano-rosetta-java/." "$chart/"
  # Keep dependency-build logs out of helm template's YAML output.
  helm dependency build "$chart" >&2
  if [[ "$1" == upgrade ]]; then
    # Preserve the installed claim configuration, including when a PVC was expanded.
    # A fresh install instead uses the candidate chart's shipped K3s defaults.
    storage_values=$(helm get values "$HELM_RELEASE" --namespace "$NAMESPACE" --all --output json |
      jq -ce '.global.storage') || return
    storage_args=(--set-json "global.storage=$storage_values")
  fi
  helm "$@" "$HELM_RELEASE" "$chart" \
    --namespace "$NAMESPACE" \
    --values "$chart/values-k3s.yaml" \
    --set-string global.network=mainnet \
    --set-string global.profile=mid \
    --set-string global.releaseVersion="$PRERELEASE_TAG" \
    --set-string global.apiImage="$API_IMAGE" \
    --set-string global.indexerImage="$INDEXER_IMAGE" \
    --set-string global.cardanoNodeImage="$CARDANO_NODE_IMAGE" \
    --set-string global.mithrilImage="$MITHRIL_IMAGE" \
    --set-string global.pgVersionTag="$postgres_version_ref" \
    --set-string global.db.existingSecret="$DB_SECRET_NAME" \
    "${storage_args[@]}" \
    --set-string rosetta-api.env.removeSpentUtxos=false \
    --set-string rosetta-api.env.tokenRegistryEnabled="$TOKEN_REGISTRY_ENABLED" \
    --set-string rosetta-api.env.tokenRegistryBaseUrl="$TOKEN_REGISTRY_BASE_URL" \
    --set-string yaci-indexer.env.removeSpentUtxos=false
}

verify_and_prefetch_k8s_target() {
  local rendered rendered_objects image
  rendered="$RUNNER_TEMP/release-test-k8s-${GITHUB_RUN_ID}-${GITHUB_RUN_ATTEMPT}.yaml"
  release_helm template > "$rendered"

  rendered_objects=$(kubectl create \
    --dry-run=client \
    --validate=false \
    --filename "$rendered" \
    --output json | jq -s '.')
  if ! jq -e \
    --arg release "$HELM_RELEASE" \
    --arg api "$API_IMAGE" \
    --arg indexer "$INDEXER_IMAGE" \
    --arg node "$CARDANO_NODE_IMAGE" \
    --arg mithril "$MITHRIL_IMAGE" \
    --arg postgres "$POSTGRES_IMAGE" '
      def resource($kind; $name):
        .[] | select(.kind == $kind and .metadata.name == $name);
      (resource("Deployment"; ($release + "-rosetta-api")) |
        any(.spec.template.spec.containers[];
          .name == "rosetta-api" and .image == $api and
          any(.env[]; .name == "REMOVE_SPENT_UTXOS" and .value == "false")) and
        any(.spec.template.spec.initContainers[]; .name == "copy-node-config" and .image == $node)) and
      (resource("Deployment"; ($release + "-yaci-indexer")) |
        any(.spec.template.spec.containers[];
          .name == "yaci-indexer" and .image == $indexer and
          any(.env[]; .name == "REMOVE_SPENT_UTXOS" and .value == "false")) and
        any(.spec.template.spec.initContainers[]; .name == "wait-for-node-sync" and .image == $node) and
        any(.spec.template.spec.initContainers[]; .name == "copy-node-config" and .image == $node)) and
      (resource("StatefulSet"; ($release + "-cardano-node")) |
        any(.spec.template.spec.containers[]; .name == "cardano-node" and .image == $node) and
        any(.spec.template.spec.containers[]; .name == "cardano-submit-api" and .image == $node) and
        any(.spec.template.spec.initContainers[]; .name == "mithril-download" and .image == $mithril)) and
      (resource("StatefulSet"; ($release + "-postgresql")) |
        any(.spec.template.spec.containers[]; .name == "postgresql" and .image == $postgres))
    ' <<< "$rendered_objects"; then
    echo "Rendered chart did not preserve the immutable images and required runtime configuration." >&2
    return 1
  fi

  for image in "$API_IMAGE" "$INDEXER_IMAGE" "$CARDANO_NODE_IMAGE" "$MITHRIL_IMAGE" "$POSTGRES_IMAGE"; do
    sudo k3s ctr images pull "docker.io/${image}"
  done
}

wait_for_k8s_target_live() {
  local api_ip endpoint
  api_ip=$(kubectl get service "$HELM_RELEASE-rosetta-api" \
    --namespace "$NAMESPACE" -o jsonpath='{.spec.clusterIP}')
  endpoint="http://${api_ip}:8082"

  source "$RELEASE_SCRIPTS/release_test_runtime.sh"
  wait_for_release_live "$endpoint" mainnet "${PRERELEASE_TAG%-pre-release}"

  verify_release_deployment rosetta-api "$API_IMAGE" copy-node-config
  verify_release_deployment yaci-indexer "$INDEXER_IMAGE" wait-for-node-sync copy-node-config
  load_release_workload statefulset cardano-node
  verify_release_container cardano-node "$CARDANO_NODE_IMAGE"
  verify_release_container cardano-submit-api "$CARDANO_NODE_IMAGE"
  verify_release_init_container mithril-download "$MITHRIL_IMAGE"
  load_release_workload statefulset postgresql
  verify_release_container postgresql "$POSTGRES_IMAGE"
  wait_for_mainnet_token_metadata "$endpoint"
}

list_k8s_release_paths() {
  local pvc=$1
  sudo find "$K8S_STORAGE_ROOT" \
    -mindepth 1 \
    -maxdepth 1 \
    -type d \
    -name "pvc-*_${NAMESPACE}_${pvc}" \
    -print
}

verify_k8s_release_path() {
  local pvc=$1 data_path=$2 basename path_uid root_device path_device path_identity
  basename=${data_path##*/}
  if [[ "$data_path" != "$K8S_STORAGE_ROOT"/* ]]; then
    echo "Release local-path is outside $K8S_STORAGE_ROOT: $data_path" >&2
    return 1
  fi
  if [[ "$basename" =~ ^pvc-([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})_${NAMESPACE}_${pvc}$ ]]; then
    path_uid=${BASH_REMATCH[1]}
  else
    echo "Unexpected local-path name for $pvc: $data_path" >&2
    return 1
  fi
  if ! sudo test -d "$data_path" || sudo test -L "$data_path" ||
     [[ "$(sudo realpath -e "$data_path")" != "$data_path" ]]; then
    echo "Unsafe release local-path for $pvc: $data_path" >&2
    return 1
  fi
  root_device=$(stat -c '%d' "$K8S_STORAGE_ROOT") || return
  path_device=$(sudo stat -c '%d' "$data_path") || return
  path_identity=$(sudo stat -c '%d:%i' "$data_path") || return
  if [[ "$path_device" != "$root_device" ]]; then
    echo "Release local-path crosses filesystems: $data_path" >&2
    return 1
  fi
  printf '%s\t%s\n' "$path_uid" "$path_identity"
}

reset_current_k8s_release_data() {
  local records paths release_pvcs unexpected_pvc volume prefix pvc pvc_json pvc_uid bound_pv identity
  local pv pv_uid data_path host_mounts mount release_name resource resource_name
  local discovered_paths candidate_path path_record path_uid path_identity path_found
  local remaining_workloads current_pvc_json current_uid current_identity current_pv current_pv_uid current_data_path
  local current_path_record current_path_uid current_path_identity referenced_pvs
  local deadline remaining_pvcs remaining_pvs remaining_paths free_bytes free_gib minimum_free_bytes pv_json
  local unreferenced_path_observations=0
  local -a cleanup_data_paths=()

  source "$RELEASE_SCRIPTS/release_test_runtime.sh" || return
  require_release_host || return
  records="$RUNNER_TEMP/clean-resync-volumes-${GITHUB_RUN_ID}-${GITHUB_RUN_ATTEMPT}.tsv"
  paths="$RUNNER_TEMP/clean-resync-paths-${GITHUB_RUN_ID}-${GITHUB_RUN_ATTEMPT}.tsv"
  : > "$records" || return
  : > "$paths" || return
  release_pvcs=$(kubectl get persistentvolumeclaims \
    --namespace "$NAMESPACE" \
    --selector "app.kubernetes.io/instance=$HELM_RELEASE" \
    --output json) || return
  unexpected_pvc=$(jq -r \
    --arg node "$NODE_PVC" \
    --arg postgres "$POSTGRES_PVC" '
      [.items[].metadata.name | select(. != $node and . != $postgres)] | first // ""
    ' <<< "$release_pvcs") || return
  if [[ -n "$unexpected_pvc" ]]; then
    echo "Unexpected PVC owned by $HELM_RELEASE: $unexpected_pvc" >&2
    return 1
  fi

  for volume in "node|$NODE_PVC" "postgres|$POSTGRES_PVC"; do
    IFS='|' read -r prefix pvc <<< "$volume"
    data_path=
    pvc_json=$(kubectl get pvc "$pvc" \
      --namespace "$NAMESPACE" \
      --ignore-not-found \
      --output json) || return
    if [[ -z "$pvc_json" ]]; then
      echo "$pvc is already absent."
    else
      pvc_uid=$(jq -er '.metadata.uid' <<< "$pvc_json") || return
      bound_pv=$(jq -r '.spec.volumeName // ""' <<< "$pvc_json") || return
      if [[ -z "$bound_pv" ]]; then
        verify_release_unbound_pvc "$pvc_json" "$pvc_uid" || return
        printf '%s\t%s\t%s\t-\t-\t-\n' \
          "$prefix" "$pvc" "$pvc_uid" >> "$records"
      else
        identity=$(verify_release_volume "$pvc" "$pvc_uid") || return
        IFS=$'\t' read -r pv pv_uid data_path <<< "$identity"
        printf '%s\t%s\t%s\t%s\t%s\t%s\n' \
          "$prefix" "$pvc" "$pvc_uid" "$pv" "$pv_uid" "$data_path" >> "$records"
      fi
    fi

    discovered_paths=$(list_k8s_release_paths "$pvc") || return
    path_found=false
    while IFS= read -r candidate_path; do
      [[ -n "$candidate_path" ]] || continue
      path_record=$(verify_k8s_release_path "$pvc" "$candidate_path") || return
      IFS=$'\t' read -r path_uid path_identity <<< "$path_record"
      printf '%s\t%s\t%s\t%s\t%s\n' \
        "$prefix" "$pvc" "$candidate_path" "$path_uid" "$path_identity" >> "$paths"
      if [[ -n "$data_path" && "$candidate_path" == "$data_path" ]]; then
        [[ "$path_uid" == "$pvc_uid" ]] || {
          echo "PVC UID does not match its local-path name: $pvc" >&2
          return 1
        }
        path_found=true
      fi
    done <<< "$discovered_paths"
    if [[ -n "$data_path" && "$path_found" != true ]]; then
      echo "Bound local-path was not found during storage discovery: $data_path" >&2
      return 1
    fi
  done

  host_mounts=$(
    findmnt --json --output TARGET |
      jq -er '.filesystems[] | recurse(.children[]?) | .target'
  ) || return
  while IFS=$'\t' read -r prefix pvc data_path path_uid path_identity; do
    [[ -n "$prefix" ]] || continue
    cleanup_data_paths+=("$data_path")
    while IFS= read -r mount; do
      if [[ "$mount" == "$data_path" || "$mount" == "$data_path"/* ]]; then
        echo "Persistent-volume path contains host mount $mount: $prefix" >&2
        return 1
      fi
    done <<< "$host_mounts"
  done < "$paths"

  require_release_cleanup_space \
    "$K8S_STORAGE_ROOT" "$K8S_MIN_FREE_GIB" "${cleanup_data_paths[@]}" || return

  release_name=$(helm list \
    --all \
    --namespace "$NAMESPACE" \
    --filter "^${HELM_RELEASE}$" \
    --short) || return
  if [[ -n "$release_name" ]]; then
    for resource in \
      statefulset/"$HELM_RELEASE-cardano-node" \
      statefulset/"$HELM_RELEASE-postgresql" \
      deployment/"$HELM_RELEASE-rosetta-api" \
      deployment/"$HELM_RELEASE-yaci-indexer"; do
      resource_name=$(kubectl get "$resource" \
        --namespace "$NAMESPACE" \
        --ignore-not-found \
        --output name) || return
      if [[ -n "$resource_name" ]]; then
        kubectl scale "$resource" --replicas=0 --namespace "$NAMESPACE" || return
      fi
    done
    helm uninstall "$HELM_RELEASE" \
      --namespace "$NAMESPACE" \
      --wait \
      --timeout 30m || return
  else
    remaining_workloads=$(kubectl get deployment,statefulset \
      --namespace "$NAMESPACE" \
      --selector "app.kubernetes.io/instance=$HELM_RELEASE" \
      --output name) || return
    if [[ -n "$remaining_workloads" ]]; then
      echo "Release-owned workloads exist without Helm release state." >&2
      return 1
    fi
  fi

  while IFS=$'\t' read -r prefix pvc pvc_uid pv pv_uid data_path; do
    [[ -n "$prefix" ]] || continue
    current_pvc_json=$(kubectl get pvc "$pvc" \
      --namespace "$NAMESPACE" \
      --ignore-not-found \
      --output json) || return
    if [[ -z "$current_pvc_json" ]]; then
      echo "$pvc disappeared after release removal; its local-path remains tracked."
      continue
    fi
    current_uid=$(jq -r '.metadata.uid' <<< "$current_pvc_json") || return
    if [[ "$current_uid" != "$pvc_uid" ]]; then
      echo "PVC identity changed immediately before deletion: $pvc" >&2
      return 1
    fi
    if [[ "$pv" == - ]]; then
      verify_release_unbound_pvc "$current_pvc_json" "$pvc_uid" || return
    else
      current_identity=$(verify_release_volume "$pvc" "$pvc_uid") || return
      IFS=$'\t' read -r current_pv current_pv_uid current_data_path <<< "$current_identity"
      if [[ "$current_pv" != "$pv" || "$current_pv_uid" != "$pv_uid" ||
            "$current_data_path" != "$data_path" ]]; then
        echo "Persistent-volume identity changed immediately before deletion: $pvc" >&2
        return 1
      fi
    fi
    if ! kubectl delete \
      --raw="/api/v1/namespaces/${NAMESPACE}/persistentvolumeclaims/${pvc}" \
      --filename=- <<EOF
{
  "apiVersion": "v1",
  "kind": "DeleteOptions",
  "preconditions": {"uid": "$pvc_uid"}
}
EOF
    then
      return 1
    fi
  done < "$records"

  minimum_free_bytes=$((K8S_MIN_FREE_GIB * 1024 * 1024 * 1024))
  deadline=$((SECONDS + 7200))
  while (( SECONDS < deadline )); do
    pvc_json=$(kubectl get persistentvolumeclaims \
      --namespace "$NAMESPACE" --output json) || return
    remaining_pvcs=$(jq \
      --arg node "$NODE_PVC" \
      --arg postgres "$POSTGRES_PVC" '
        [.items[] | select(.metadata.name == $node or .metadata.name == $postgres)] | length
      ' <<< "$pvc_json") || return
    pv_json=$(kubectl get persistentvolumes --output json) || return
    remaining_pvs=$(jq \
      --arg namespace "$NAMESPACE" \
      --arg node "$NODE_PVC" \
      --arg postgres "$POSTGRES_PVC" '
        [.items[] | select(
          .spec.claimRef.namespace == $namespace and
          (.spec.claimRef.name == $node or .spec.claimRef.name == $postgres)
        )] | length
      ' <<< "$pv_json") || return
    remaining_paths=0
    while IFS=$'\t' read -r prefix pvc data_path path_uid path_identity; do
      [[ -n "$prefix" ]] || continue
      if sudo test -e "$data_path"; then
        (( remaining_paths += 1 ))
      fi
    done < "$paths"
    free_bytes=$(df --output=avail -B1 "$K8S_STORAGE_ROOT" |
      awk 'NR == 2 {print $1}') || return
    [[ "$free_bytes" =~ ^[0-9]+$ ]] || {
      echo "Could not measure free space after K8s cleanup." >&2
      return 1
    }
    free_gib=$((free_bytes / 1024 / 1024 / 1024))
    if (( remaining_pvcs == 0 && remaining_pvs == 0 && remaining_paths == 0 )); then
      if (( free_bytes < minimum_free_bytes )); then
        echo "K8s reset left $free_bytes bytes free (${free_gib}GiB); at least ${K8S_MIN_FREE_GIB}GiB is required before installation." >&2
        return 1
      fi
      echo "Current K8s release data removed with ${free_gib}GiB available."
      return 0
    fi

    if (( remaining_pvcs == 0 && remaining_pvs == 0 && remaining_paths > 0 )); then
      (( unreferenced_path_observations += 1 ))
      if (( unreferenced_path_observations >= 2 )); then
        while IFS=$'\t' read -r prefix pvc data_path path_uid path_identity; do
          [[ -n "$prefix" ]] || continue
          sudo test -e "$data_path" || continue
          pv_json=$(kubectl get persistentvolumes --output json) || return
          referenced_pvs=$(jq --arg path "$data_path" '
            [.items[] | select(
              (.spec.local.path // .spec.hostPath.path // "") == $path
            )] | length
          ' <<< "$pv_json") || return
          if (( referenced_pvs != 0 )); then
            echo "Refusing orphan cleanup of a referenced local-path: $data_path" >&2
            return 1
          fi

          host_mounts=$(
            findmnt --json --output TARGET |
              jq -er '.filesystems[] | recurse(.children[]?) | .target'
          ) || return
          while IFS= read -r mount; do
            if [[ "$mount" == "$data_path" || "$mount" == "$data_path"/* ]]; then
              echo "Refusing orphan cleanup across host mount $mount under $data_path." >&2
              return 1
            fi
          done <<< "$host_mounts"
          current_path_record=$(verify_k8s_release_path \
            "$pvc" "$data_path") || return
          IFS=$'\t' read -r current_path_uid current_path_identity <<< "$current_path_record"
          if [[ "$current_path_uid" != "$path_uid" || "$current_path_identity" != "$path_identity" ]]; then
            echo "Local-path identity changed at the orphan deletion boundary: $data_path" >&2
            return 1
          fi

          sudo find "$data_path" -xdev -mindepth 1 -delete || return
          sudo rmdir "$data_path" || return
          echo "Removed unreferenced release local-path: $data_path"
        done < "$paths"
        continue
      fi
    else
      unreferenced_path_observations=0
    fi

    echo "Waiting for K8s cleanup: pvc=$remaining_pvcs pv=$remaining_pvs paths=$remaining_paths free=${free_gib}GiB."
    sleep 30
  done

  echo "PVC deletion did not remove the bound PVs and paths." >&2
  return 1
}
