#!/usr/bin/env bash
# k3s-setup.sh — Deploy Cardano Rosetta Java to a local K3s cluster
#
# Usage:
#   ./scripts/k3s-setup.sh [preprod|mainnet] [namespace]
#
# Environment variables:
#   DB_PASSWORD   PostgreSQL password (default: weakpwd#123_d for local dev)
#   KUBECONFIG    Path to kubeconfig (auto-detected from k3s if not set)

set -euo pipefail

PROFILE="${1:-preprod}"
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

# ── Validate profile ────────────────────────────────────────────────────────
case "$PROFILE" in
  preprod|mainnet) info "Profile: ${PROFILE}" ;;
  *) error "Unknown profile '${PROFILE}'. Expected: preprod | mainnet" ;;
esac

# ── Build values file list ──────────────────────────────────────────────────
BASE_VALUES="-f ${CHART_DIR}/values.yaml"
case "$PROFILE" in
  preprod)
    PROFILE_VALUES="-f ${CHART_DIR}/values-preprod.yaml -f ${CHART_DIR}/values-k3s.yaml"
    ;;
  mainnet)
    PROFILE_VALUES="-f ${CHART_DIR}/values-k3s.yaml"
    ;;
esac

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
  if command -v "$cmd" &>/dev/null; then
    info "  ✓ ${cmd} $(${cmd} version --short 2>/dev/null | head -1)"
  else
    error "${cmd} is required but not installed. See: https://helm.sh/docs/intro/install/"
  fi
done

# Helm v3 check
HELM_MAJOR=$(helm version --short 2>/dev/null | grep -oP 'v\K[0-9]+' | head -1 || echo 0)
[ "${HELM_MAJOR:-0}" -ge 3 ] || error "Helm v3 or higher is required"

# Verify cluster connectivity
kubectl cluster-info &>/dev/null || error "Cannot connect to Kubernetes cluster. Check KUBECONFIG."
info "  ✓ Cluster is reachable"

# ── Namespace ───────────────────────────────────────────────────────────────
step "Creating namespace '${NAMESPACE}'"
kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -
info "Namespace '${NAMESPACE}' ready"

# ── Helm dependency build ───────────────────────────────────────────────────
step "Building Helm chart dependencies"
helm dependency build "${CHART_DIR}"

# ── Lint ────────────────────────────────────────────────────────────────────
step "Linting Helm chart"
helm lint "${CHART_DIR}" \
  ${BASE_VALUES} \
  ${PROFILE_VALUES} \
  --set global.db.password="${DB_PASSWORD}" \
  --quiet && info "Lint passed"

# ── Deploy ──────────────────────────────────────────────────────────────────
step "Deploying '${RELEASE_NAME}' (profile: ${PROFILE})"
warn "First-time deployment takes 30–90 minutes:"
warn "  • Mithril snapshot download: 1–4 hours (preprod: ~30 min)"
warn "  • Node sync to tip: 10–30 minutes"
warn "  • APPLYING_INDEXES stage: ~6 hours on mainnet"
echo ""

helm upgrade --install "${RELEASE_NAME}" "${CHART_DIR}" \
  --namespace "${NAMESPACE}" \
  --create-namespace \
  ${BASE_VALUES} \
  ${PROFILE_VALUES} \
  --set global.db.password="${DB_PASSWORD}" \
  --timeout 90m \
  --wait \
  --wait-for-jobs \
  --atomic

# ── Success ─────────────────────────────────────────────────────────────────
echo ""
info "════════════════════════════════════════════════"
info "  Deployment successful!"
info "════════════════════════════════════════════════"
echo ""
info "Useful commands:"
echo "  make k8s-status             # pod status"
echo "  make k8s-logs-node          # cardano-node logs"
echo "  make k8s-logs-indexer       # yaci-indexer logs"
echo "  make k8s-logs-api           # rosetta-api logs"
echo "  make k8s-port-forward       # forward API to localhost:8082"
echo "  make k8s-test               # run Helm tests"
echo ""
info "Check sync status (after port-forward):"
echo "  curl -s http://localhost:8082/network/status \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"network_identifier\":{\"blockchain\":\"cardano\",\"network\":\"${PROFILE}\"}}' \\"
echo "    -X POST | jq '.sync_status'"
