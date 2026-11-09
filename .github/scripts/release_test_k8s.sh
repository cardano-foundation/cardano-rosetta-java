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

reinstall_helm_release() {
  local workloads before after identity
  local -a resources=("pvc/node-data-$HELM_RELEASE-cardano-node-0"
    "pvc/pg-data-$HELM_RELEASE-postgresql-0" "secret/$DB_SECRET_NAME")

  workloads=$(kubectl get statefulset "$HELM_RELEASE-cardano-node" "$HELM_RELEASE-postgresql" \
    --namespace "$NAMESPACE" --output json) || return
  jq -e 'all(.items[];
    (.spec.persistentVolumeClaimRetentionPolicy.whenDeleted // "Retain") == "Retain")
  ' <<< "$workloads" || { echo "StatefulSets must retain PVCs on uninstall." >&2; return 1; }
  verify_release_database_secret || return

  # Compare the same claims, bound volumes and credentials once after reinstall.
  identity='[.items[] |
    if .metadata.deletionTimestamp != null or ((.metadata.ownerReferences // []) | length) != 0
    then error("Resource is being deleted or has a garbage-collection owner")
    else {kind, name: .metadata.name, uid: .metadata.uid,
          volume: .spec.volumeName, credentials: .data} end
  ] | sort_by(.kind, .name)'
  before=$(kubectl get "${resources[@]}" --namespace "$NAMESPACE" --output json |
    jq -cSe "$identity") || return

  helm uninstall "$HELM_RELEASE" --namespace "$NAMESPACE" \
    --cascade=foreground --wait --timeout 30m || return
  helm install "$@" || return

  after=$(kubectl get "${resources[@]}" --namespace "$NAMESPACE" --output json |
    jq -cSe "$identity") || return
  [[ "$after" == "$before" ]] || {
    echo "PVC identity, bound volume or database credentials changed during reinstall." >&2
    return 1
  }
}

release_helm() {
  local postgres_version_ref=${POSTGRES_IMAGE#cardanofoundation/cardano-rosetta-java-postgres:}
  local chart storage_values
  local -a storage_args=() helm_args=()
  chart=$(mktemp -d "$RUNNER_TEMP/release-chart.XXXXXX") || return
  cp -a "$DEPLOY_DIR/helm/cardano-rosetta-java/." "$chart/" || return
  if [[ "$1" == reinstall || "${K8S_UPGRADE:-false}" == true ]]; then
    # Preserve installed storage on reinstall; fresh installs use chart defaults.
    storage_values=$(helm get values "$HELM_RELEASE" --namespace "$NAMESPACE" --all --output json |
      jq -ce '.global.storage') || return
    storage_args=(--set-json "global.storage=$storage_values")
  fi
  # Keep dependency-build logs out of helm template's YAML output.
  helm dependency build "$chart" >&2 || return
  helm_args=("$HELM_RELEASE" "$chart" \
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
    --set-string yaci-indexer.env.removeSpentUtxos=false)
  if [[ "$1" == reinstall ]]; then
    shift
    reinstall_helm_release "$@" "${helm_args[@]}"
  else
    helm "$@" "${helm_args[@]}"
  fi
}

verify_and_prefetch_k8s_target() {
  local rendered rendered_objects image
  rendered="$RUNNER_TEMP/release-test-k8s-${GITHUB_RUN_ID}-${GITHUB_RUN_ATTEMPT}.yaml"
  K8S_UPGRADE=${1:-false} release_helm template > "$rendered"

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

stop_k8s_release() {
  local workloads pods selector="app.kubernetes.io/instance=$HELM_RELEASE"
  local -a pod_names=()
  source "$RELEASE_SCRIPTS/release_test_runtime.sh" || return
  require_release_host || return
  verify_release_cluster || return
  workloads=$(kubectl get deployment,statefulset --namespace "$NAMESPACE" \
    --selector "$selector" --output json) || return
  jq -e --arg release "$HELM_RELEASE" --arg namespace "$NAMESPACE" '
    all(.items[];
      .metadata.annotations["meta.helm.sh/release-name"] == $release and
      .metadata.annotations["meta.helm.sh/release-namespace"] == $namespace and
      (.kind != "StatefulSet" or
       (.spec.persistentVolumeClaimRetentionPolicy.whenScaled // "Retain") == "Retain"))
  ' <<< "$workloads" || { echo "Unsafe release workload ownership or PVC retention." >&2; return 1; }
  [[ "$(jq '.items | length' <<< "$workloads")" != 0 ]] || return 0
  kubectl scale deployment,statefulset --namespace "$NAMESPACE" \
    --selector "$selector" --replicas=0 || return
  pods=$(kubectl get pods --namespace "$NAMESPACE" --selector "$selector" --output name) || return
  [[ -n "$pods" ]] || return 0
  mapfile -t pod_names <<< "$pods"
  kubectl wait --namespace "$NAMESPACE" --for=delete --timeout=10m "${pod_names[@]}"
}

reset_current_k8s_release_data() {
  local claims pv_names pv policy
  local -a volumes=()

  claims=$(kubectl get pvc "$NODE_PVC" "$POSTGRES_PVC" --namespace "$NAMESPACE" \
    --ignore-not-found --output json) || return
  claims=${claims:-'{"items":[]}'}
  jq -e --arg release "$HELM_RELEASE" '
    all(.items[]; .metadata.labels["app.kubernetes.io/instance"] == $release)
  ' <<< "$claims" || { echo "Refusing to reset another release's PVCs." >&2; return 1; }

  pv_names=$(jq -r '.items[].spec.volumeName // empty' <<< "$claims") || return
  for pv in $pv_names; do
    policy=$(kubectl get pv "$pv" --output jsonpath='{.spec.persistentVolumeReclaimPolicy}') || return
    [[ "$policy" == Delete ]] || { echo "$pv must use the Delete reclaim policy." >&2; return 1; }
    volumes+=("pv/$pv")
  done

  helm uninstall "$HELM_RELEASE" --namespace "$NAMESPACE" \
    --ignore-not-found --cascade=foreground --wait --timeout 30m || return
  kubectl delete pvc "$NODE_PVC" "$POSTGRES_PVC" --namespace "$NAMESPACE" \
    --ignore-not-found --wait=true --timeout=30m || return
  if (( ${#volumes[@]} )); then
    kubectl wait --for=delete --timeout=2h "${volumes[@]}" || return
  fi

  source "$RELEASE_SCRIPTS/release_test_runtime.sh" || return
  require_release_free_space "$K8S_STORAGE_ROOT" "$K8S_MIN_FREE_GIB"
}
