#!/usr/bin/env bash
# k3s-setup.sh — Deploy Cardano Rosetta Java to a local K3s cluster
#
# Usage:
#   ./scripts/k3s-setup.sh [preprod|mainnet] [namespace]
#
# Environment variables:
#   DB_PASSWORD   PostgreSQL password (required for production; defaults to weak dev password)
#   KUBECONFIG    Path to kubeconfig (auto-detected from k3s if not set)
#
# Notes:
#   - Always deploys with --no-hooks. Run the index-applier manually when the
#     indexer reaches chain tip (see docs/runbooks/deployment.md).
#   - Subcharts must be pre-packaged before first run:
#       helm package helm/cardano-rosetta-java/charts/<name> -d helm/cardano-rosetta-java/charts/
#     Or package all at once:
#       for d in helm/cardano-rosetta-java/charts/*/; do
#         helm package "$d" -d helm/cardano-rosetta-java/charts/
#       done

set -euo pipefail

NETWORK="${1:-preprod}"
NAMESPACE="${2:-cardano}"
RELEASE_NAME="rosetta"
CHART_DIR="./helm/cardano-rosetta-java"
DB_PASSWORD="${DB_PASSWORD:-weakpwd#123_d}"

# ── Colour helpers ──────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }
step()  { echo -e "\n${CYAN}▶ $*${NC}"; }

# ── Validate network ─────────────────────────────────────────────────────────
case "$NETWORK" in
  preprod)
    PROTOCOL_MAGIC=1
    PROFILE_VALUES="-f ${CHART_DIR}/values-preprod.yaml -f ${CHART_DIR}/values-k3s.yaml"
    ;;
  mainnet)
    PROTOCOL_MAGIC=764824073
    PROFILE_VALUES="-f ${CHART_DIR}/values-k3s.yaml"
    ;;
  *) error "Unknown network '${NETWORK}'. Expected: preprod | mainnet" ;;
esac

info "Network: ${NETWORK} (protocolMagic=${PROTOCOL_MAGIC})"

# ── Install k3s if not present ──────────────────────────────────────────────
step "Checking k3s / kubectl availability"
if ! command -v kubectl &>/dev/null; then
  if ! command -v k3s &>/dev/null; then
    info "Neither kubectl nor k3s found — installing k3s..."
    if [ "$(id -u)" -ne 0 ]; then
      error "k3s installation requires root. Run as root or pre-install k3s."
    fi
    curl -sfL https://get.k3s.io | sh -s - --write-kubeconfig-mode 644
    info "Waiting for k3s API server..."
    sleep 15
    until /usr/local/bin/kubectl --kubeconfig /etc/rancher/k3s/k3s.yaml get nodes &>/dev/null; do
      echo "  Waiting..."
      sleep 5
    done
    info "k3s is running"
  fi
fi

# ── Configure KUBECONFIG ────────────────────────────────────────────────────
if [ -z "${KUBECONFIG:-}" ]; then
  if [ -f /etc/rancher/k3s/k3s.yaml ]; then
    export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
    info "Using KUBECONFIG=${KUBECONFIG}"
  else
    warn "KUBECONFIG not set and /etc/rancher/k3s/k3s.yaml not found."
    warn "Ensure KUBECONFIG points to your cluster configuration."
  fi
fi

# ── Prerequisites ───────────────────────────────────────────────────────────
step "Verifying prerequisites"
for cmd in kubectl helm; do
  command -v "$cmd" &>/dev/null || error "${cmd} is required but not installed."
done

HELM_MAJOR=$(helm version --template '{{.Version}}' 2>/dev/null | grep -oP 'v\K[0-9]+' | head -1 || echo 0)
[ "${HELM_MAJOR:-0}" -ge 3 ] || error "Helm v3 or higher is required"
info "  ✓ kubectl $(kubectl version --client -o yaml 2>/dev/null | grep 'gitVersion' | head -1 | awk '{print $2}')"
info "  ✓ helm $(helm version --short 2>/dev/null | head -1)"

kubectl cluster-info &>/dev/null || error "Cannot connect to Kubernetes cluster. Check KUBECONFIG."
info "  ✓ Cluster is reachable"

# ── Namespace ───────────────────────────────────────────────────────────────
# NOTE: Do NOT use --create-namespace — the chart has templates/namespace.yaml
# which creates it with the correct Helm ownership labels.
step "Ensuring namespace '${NAMESPACE}' exists"
if kubectl get namespace "${NAMESPACE}" &>/dev/null 2>&1; then
  info "Namespace '${NAMESPACE}' already exists"
else
  info "Pre-creating namespace '${NAMESPACE}' with Helm ownership labels..."
  kubectl create namespace "${NAMESPACE}"
  kubectl label namespace "${NAMESPACE}" \
    "app.kubernetes.io/managed-by=Helm"
  kubectl annotate namespace "${NAMESPACE}" \
    "meta.helm.sh/release-name=${RELEASE_NAME}" \
    "meta.helm.sh/release-namespace=${NAMESPACE}"
  info "Namespace '${NAMESPACE}' created"
fi

# ── Lint ────────────────────────────────────────────────────────────────────
step "Linting Helm chart"
LINT_OUT=$(helm lint "${CHART_DIR}" \
  -f "${CHART_DIR}/values.yaml" \
  ${PROFILE_VALUES} \
  --set global.network="${NETWORK}" \
  --set global.protocolMagic="${PROTOCOL_MAGIC}" \
  "--set=global.db.password=${DB_PASSWORD}" \
  --quiet 2>&1 || true)
echo "${LINT_OUT}" | grep -v "walk.go" || true
echo "${LINT_OUT}" | grep -q "ERROR" && error "Helm lint failed" || info "Lint passed"

# ── Deploy ──────────────────────────────────────────────────────────────────
step "Deploying '${RELEASE_NAME}' (network: ${NETWORK})"
warn "Deployment notes:"
warn "  • Mithril snapshot download: 1–4 hours (preprod: ~30 min)"
warn "  • Node sync to tip: 10–60 minutes after Mithril"
warn "  • Index-applier NOT started (--no-hooks). Run manually when synced."
warn "    See: docs/runbooks/deployment.md"
echo ""

# IMPORTANT: --no-hooks prevents the index-applier from running during deploy.
# The index-applier must be triggered separately once the indexer reaches chain tip.
# See: docs/runbooks/deployment.md for the manual index-applier workflow.
helm upgrade --install "${RELEASE_NAME}" "${CHART_DIR}" \
  --namespace "${NAMESPACE}" \
  -f "${CHART_DIR}/values.yaml" \
  ${PROFILE_VALUES} \
  --set global.network="${NETWORK}" \
  --set global.protocolMagic="${PROTOCOL_MAGIC}" \
  "--set=global.db.password=${DB_PASSWORD}" \
  --no-hooks \
  2>&1 | grep -v "walk.go"

# ── Success ─────────────────────────────────────────────────────────────────
echo ""
info "════════════════════════════════════════════════"
info "  Deployment submitted!"
info "════════════════════════════════════════════════"
echo ""
info "Monitor sync progress:"
echo "  export KUBECONFIG=${KUBECONFIG:-/etc/rancher/k3s/k3s.yaml}"
echo "  kubectl get pods -n ${NAMESPACE}"
echo "  kubectl logs -f statefulset/rosetta-cardano-node -c cardano-node -n ${NAMESPACE}"
echo "  kubectl logs -f deployment/rosetta-yaci-indexer -n ${NAMESPACE}"
echo ""
info "When indexer reaches chain tip, apply DB indexes:"
echo "  helm upgrade ${RELEASE_NAME} ${CHART_DIR} \\"
echo "    --namespace ${NAMESPACE} \\"
echo "    -f ${CHART_DIR}/values.yaml \\"
echo "    ${PROFILE_VALUES} \\"
echo "    --set global.network=${NETWORK} \\"
echo "    --set global.protocolMagic=${PROTOCOL_MAGIC} \\"
echo "    \"--set=global.db.password=\${DB_PASSWORD}\""
echo "    # (without --no-hooks — lets index-applier run)"
