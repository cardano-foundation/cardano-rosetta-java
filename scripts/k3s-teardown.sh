#!/usr/bin/env bash
# k3s-teardown.sh — Uninstall Cardano Rosetta Java from K3s
#
# Usage:
#   ./scripts/k3s-teardown.sh [--uninstall-k3s] [--delete-pvcs]
#
# Flags:
#   --uninstall-k3s   Also uninstall k3s from the host (requires root)
#   --delete-pvcs     Also delete PVCs (DESTRUCTIVE — permanently deletes blockchain data)

set -euo pipefail

RELEASE_NAME="rosetta"
NAMESPACE="cardano"
UNINSTALL_K3S=false
DELETE_PVCS=false

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }
step()  { echo -e "\n${CYAN}▶ $*${NC}"; }

# ── Parse flags ─────────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --uninstall-k3s) UNINSTALL_K3S=true ;;
    --delete-pvcs)   DELETE_PVCS=true ;;
    --help|-h)
      cat <<EOF
Usage: $0 [OPTIONS]

Options:
  --uninstall-k3s   Uninstall k3s from the host (requires root, DESTRUCTIVE)
  --delete-pvcs     Delete all PVCs in namespace (DESTRUCTIVE — destroys blockchain data)
  --help            Show this help

Examples:
  $0                          # Uninstall Helm release only (PVCs retained)
  $0 --delete-pvcs            # Uninstall + delete all data (full cleanup)
  $0 --uninstall-k3s --delete-pvcs  # Complete teardown
EOF
      exit 0
      ;;
    *) error "Unknown flag: $1 (use --help for usage)" ;;
  esac
  shift
done

# ── Configure KUBECONFIG ────────────────────────────────────────────────────
if [ -z "${KUBECONFIG:-}" ] && [ -f /etc/rancher/k3s/k3s.yaml ]; then
  export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
fi

# ── Prerequisite checks ─────────────────────────────────────────────────────
for cmd in helm kubectl; do
  command -v "$cmd" &>/dev/null || error "${cmd} is required"
done

# ── Uninstall Helm release ──────────────────────────────────────────────────
step "Uninstalling Helm release"
if helm status "${RELEASE_NAME}" -n "${NAMESPACE}" &>/dev/null 2>&1; then
  info "Uninstalling '${RELEASE_NAME}' from namespace '${NAMESPACE}'..."
  helm uninstall "${RELEASE_NAME}" -n "${NAMESPACE}" --wait --timeout 10m
  info "Helm release uninstalled"
else
  warn "Release '${RELEASE_NAME}' not found in '${NAMESPACE}', skipping"
fi

# ── Handle PVCs ─────────────────────────────────────────────────────────────
step "PersistentVolumeClaims"
PVCS=$(kubectl get pvc -n "${NAMESPACE}" --no-headers 2>/dev/null | awk '{print $1}' || true)

if [ -z "$PVCS" ]; then
  info "No PVCs found in namespace '${NAMESPACE}'"
elif [ "$DELETE_PVCS" = "true" ]; then
  warn "The following PVCs will be PERMANENTLY DELETED:"
  echo "$PVCS" | while read -r pvc; do echo "    - $pvc"; done
  warn "This includes all Cardano blockchain data and PostgreSQL data!"
  echo ""
  read -r -p "Type 'yes-delete-all-data' to confirm: " CONFIRM
  if [ "$CONFIRM" = "yes-delete-all-data" ]; then
    kubectl delete pvc --all -n "${NAMESPACE}" --ignore-not-found=true
    info "All PVCs deleted"
  else
    info "PVC deletion cancelled — data preserved"
  fi
else
  info "PVCs retained (re-run with --delete-pvcs to also delete blockchain data):"
  kubectl get pvc -n "${NAMESPACE}" 2>/dev/null | sed 's/^/  /' || true
fi

# ── Delete namespace ─────────────────────────────────────────────────────────
step "Removing namespace"
kubectl delete namespace "${NAMESPACE}" --ignore-not-found=true --timeout=60s 2>/dev/null || true
info "Namespace '${NAMESPACE}' removed"

# ── Uninstall k3s ───────────────────────────────────────────────────────────
if [ "$UNINSTALL_K3S" = "true" ]; then
  step "Uninstalling k3s"
  if command -v k3s-uninstall.sh &>/dev/null; then
    k3s-uninstall.sh
    info "k3s uninstalled"
  elif [ -f /usr/local/bin/k3s-uninstall.sh ]; then
    /usr/local/bin/k3s-uninstall.sh
    info "k3s uninstalled"
  else
    error "k3s-uninstall.sh not found. Uninstall k3s manually."
  fi
fi

echo ""
info "Teardown complete"
