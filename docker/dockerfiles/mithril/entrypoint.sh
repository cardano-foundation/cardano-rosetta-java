#!/bin/bash

download_mithril_snapshot() {
    echo "Downloading Mithril Snapshot..."
    export CARDANO_NETWORK=$NETWORK
    case $NETWORK in
    mainnet)
      AGGREGATOR_ENDPOINT=${AGGREGATOR_ENDPOINT:-https://aggregator.release-mainnet.api.mithril.network/aggregator}
      GENESIS_VERIFICATION_KEY=${GENESIS_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-mainnet/genesis.vkey)}
      ANCILLARY_VERIFICATION_KEY=${ANCILLARY_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-mainnet/ancillary.vkey)}
      ;;
    preprod)
      AGGREGATOR_ENDPOINT=${AGGREGATOR_ENDPOINT:-https://aggregator.release-preprod.api.mithril.network/aggregator}
      GENESIS_VERIFICATION_KEY=${GENESIS_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-preprod/genesis.vkey)}
      ANCILLARY_VERIFICATION_KEY=${ANCILLARY_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-preprod/ancillary.vkey)}
      ;;
    preview)
      AGGREGATOR_ENDPOINT=${AGGREGATOR_ENDPOINT:-https://aggregator.pre-release-preview.api.mithril.network/aggregator}
      GENESIS_VERIFICATION_KEY=${GENESIS_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/pre-release-preview/genesis.vkey)}
      ANCILLARY_VERIFICATION_KEY=${ANCILLARY_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/pre-release-preview/ancillary.vkey)}
      ;;
    esac
    echo "Listing content of /node dir:"
    ls -la /node

    # One-time --allow-override gate. Mithril 2617.0 dropped the v1 backend; the
    # v2 backend refuses to download into a non-empty dir unless --allow-override
    # is set. We want that override exactly ONCE -- to migrate a legacy
    # (v1 / pre-v2-aware) /node -- then never again, otherwise every restart
    # would clobber an already-synced (and possibly ahead) node DB. The marker
    # is written by THIS script after a successful download, so its presence
    # means "this v2-aware entrypoint already restored here". It is a provenance
    # flag, not a snapshot-format probe. /node is a bind mount
    # (${CARDANO_NODE_DIR}:/node in docker-compose-node.yaml) so the marker
    # persists across restarts.
    MITHRIL_V2_MARKER="/node/.mithril-v2.done"
    OVERRIDE=""
    if [ -n "$(ls -A /node 2>/dev/null)" ] && [ ! -f "$MITHRIL_V2_MARKER" ]; then
        echo "Pre-existing /node data without v2 marker -> one-time --allow-override (v1->v2 migration)"
        OVERRIDE="--allow-override"
    fi

    mithril-client cardano-db download latest --include-ancillary --ancillary-verification-key $ANCILLARY_VERIFICATION_KEY --download-dir /node $OVERRIDE &
    MITHRIL_PID=$!
    wait $MITHRIL_PID
    MITHRIL_RC=$?

    # Write the marker only on a genuinely successful download. In steady state
    # (marker present, dir non-empty, no override) mithril-client exits non-zero
    # BY DESIGN -- that is expected, not a failure: we keep the existing DB. The
    # script still exits 0 (bare `exit` below) so downstream services gated on
    # service_completed_successfully are never blocked on restart.
    if [ "$MITHRIL_RC" -eq 0 ]; then
        touch "$MITHRIL_V2_MARKER"
        echo "Done downloading Mithril Snapshot (v2 marker set)"
    else
        echo "Mithril download exited $MITHRIL_RC (kept existing /node data)"
    fi
}

echo $NETWORK
if [ "${MITHRIL_SYNC}" == "true" ]; then
    download_mithril_snapshot
fi

exit