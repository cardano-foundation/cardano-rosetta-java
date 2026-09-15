#!/usr/bin/env bash
# Runtime evidence shared by the dedicated Compose and Kubernetes test deployments.

require_release_host() {
  [[ "$(hostname -f)" == "$EXPECTED_HOST" ]] || {
    echo "Expected runner host $EXPECTED_HOST, found $(hostname -f)." >&2
    return 1
  }
}

wait_for_release_live() {
  local endpoint=$1 network=$2 expected_version=$3
  local payload sync_state version attempt=0 deadline=$((SECONDS + SYNC_TIMEOUT_SECONDS))
  payload=$(jq -nc --arg network "$network" \
    '{network_identifier: {blockchain: "cardano", network: $network}}')
  until sync_state=$(curl -fs --header 'Content-Type: application/json' --data "$payload" \
      "$endpoint/network/status" | jq -r '.sync_status.synced and .sync_status.stage == "LIVE"') \
      && [[ "$sync_state" == true ]]; do
    (( SECONDS < deadline )) || { echo "$PHASE did not reach LIVE in time." >&2; exit 1; }
    (( ++attempt % 20 )) || echo "$PHASE: still waiting after $((SECONDS / 60))m."
    sleep "${POLL_INTERVAL_SECONDS:-60}"
  done
  version=$(curl -fsS --header 'Content-Type: application/json' --data "$payload" \
    "$endpoint/network/options" | jq -r '.version.middleware_version')
  [[ "$version" == "$expected_version" ]] || {
    echo "$PHASE reports middleware $version, expected $expected_version." >&2; exit 1;
  }
  echo "$PHASE is LIVE on $version."
}

wait_for_release_token_metadata() {
  local endpoint=$1 network=$2 address=$3 block=$4 policy_id=$5
  local symbol=$6 decimals=$7 ticker=$8
  local payload response metadata_matches deadline
  deadline=$((SECONDS + ${TOKEN_METADATA_TIMEOUT_SECONDS:-900}))
  payload=$(jq -nc --arg network "$network" --arg address "$address" --argjson block "$block" '
    {
      network_identifier: {blockchain: "cardano", network: $network},
      account_identifier: {address: $address},
      block_identifier: {index: $block}
    }
  ')

  while (( SECONDS < deadline )); do
    if response=$(curl -fsS \
        --connect-timeout 5 \
        --max-time 30 \
        --header 'Content-Type: application/json' \
        --data "$payload" \
        "${endpoint%/}/account/balance"); then
      metadata_matches=$(jq -r \
        --arg policy_id "$policy_id" \
        --arg symbol "$symbol" \
        --argjson decimals "$decimals" \
        --arg ticker "$ticker" '
          any(.balances[]?;
            .currency.symbol == $symbol and
            .currency.decimals == $decimals and
            .currency.metadata.policyId == $policy_id and
            .currency.metadata.ticker == $ticker
          )
        ' <<< "$response") || return
      if [[ "$metadata_matches" == true ]]; then
        echo "Rosetta API returned the expected $network token metadata enrichment."
        return 0
      fi
    fi
    sleep "${TOKEN_METADATA_POLL_SECONDS:-30}"
  done

  echo "Rosetta API did not return the expected $network token metadata before timeout." >&2
  return 1
}

wait_for_mainnet_token_metadata() {
  wait_for_release_token_metadata \
    "$1" \
    mainnet \
    addr1qxzm06p3mp85lvu9mm8g8tvtvhr4sh8crkqq4vxpxrvy5g4496n2ffvujnucp666sgsrelhzzcfl3uy9m82lqsgt6x0q3y08z7 \
    12875388 \
    279c909f348e533da5808898f87f9a14bb2c3dfbbacccd631d927a3f \
    534e454b \
    0 \
    SNEK
}

wait_for_preprod_token_metadata() {
  wait_for_release_token_metadata \
    "$1" \
    preprod \
    addr_test1qqzdcr8caujvm4kjdv3mh90xvc3gh8k3d0et4cnja47cy74aavghhj8e4rryf0xyth5yj0yu7lcxulk6rqhwfvel7p0qa8czcz \
    486800 \
    c6e65ba7878b2f8ea0ad39287d3e2fd256dc5c4160fc19bdf4c4d87e \
    7447454e53 \
    6 \
    tGENS
}

measure_release_cleanup_space() {
  local storage_root=$1 free_bytes reclaimable_bytes=0 data_path du_output used_bytes
  shift
  free_bytes=$(df --output=avail -B1 "$storage_root" |
    awk 'NR == 2 {print $1}') || return
  [[ "$free_bytes" =~ ^[0-9]+$ ]] || {
    echo "Could not measure free space under $storage_root." >&2
    return 1
  }
  for data_path in "$@"; do
    # Live files may vanish during du. Accept its total only if it is numeric.
    du_output=$(sudo du -sB1 "$data_path") || true
    used_bytes=$(awk '{print $1}' <<< "$du_output")
    [[ "$used_bytes" =~ ^[0-9]+$ ]] || {
      echo "Could not measure $data_path." >&2
      return 1
    }
    reclaimable_bytes=$((reclaimable_bytes + used_bytes))
  done
  printf '%s\t%s\t%s\n' "$free_bytes" "$reclaimable_bytes" "$((free_bytes + reclaimable_bytes))"
}

require_release_cleanup_space() {
  local storage_root=$1 minimum_free_gib=$2 measurement free_bytes reclaimable_bytes projected_bytes
  local minimum_free_bytes
  shift 2
  [[ "$minimum_free_gib" =~ ^[0-9]+$ ]] || {
    echo "Minimum free-space requirement must be an integer GiB value." >&2
    return 1
  }
  measurement=$(measure_release_cleanup_space "$storage_root" "$@") || return
  IFS=$'\t' read -r free_bytes reclaimable_bytes projected_bytes <<< "$measurement"
  minimum_free_bytes=$((minimum_free_gib * 1024 * 1024 * 1024))
  printf 'Disk space at %s: available=%s bytes; deployment data=%s bytes; estimated available after cleanup=%s bytes.\n' \
    "$storage_root" "$free_bytes" "$reclaimable_bytes" "$projected_bytes"
  if (( projected_bytes < minimum_free_bytes )); then
    echo "Projected free space is below the required ${minimum_free_gib}GiB." >&2
    return 1
  fi
}

capture_release_machine() {
  local disk_path=$1 deployment=$2 environment=$3
  local cpu_json cpu_model cores_per_socket sockets threads physical_cores
  local key value visible_ram_gib disk_total_gib disk_free_gib machine_specs
  local -a disk_stats
  cpu_json=$(lscpu --json)
  cpu_model=$(jq -r '.lscpu[] | select(.field == "Model name:") | .data' <<< "$cpu_json")
  cores_per_socket=$(jq -r '.lscpu[] | select(.field == "Core(s) per socket:") | .data' <<< "$cpu_json")
  sockets=$(jq -r '.lscpu[] | select(.field == "Socket(s):") | .data' <<< "$cpu_json")
  physical_cores=$((cores_per_socket * sockets))
  threads=$(nproc)
  while read -r key value _; do
    if [[ "$key" == "MemTotal:" ]]; then
      visible_ram_gib=$(((value + 524288) / 1048576))
      break
    fi
  done < /proc/meminfo
  mapfile -t disk_stats < <(df -BG --output=size,avail "$disk_path")
  read -r disk_total_gib disk_free_gib <<< "${disk_stats[1]}"
  disk_total_gib=${disk_total_gib%G}
  disk_free_gib=${disk_free_gib%G}
  machine_specs="${threads} vCPUs, ${visible_ram_gib}GB RAM; CPU=${cpu_model}; cores=${physical_cores}; threads=${threads}; visible RAM=${visible_ram_gib}GiB; disk=${disk_total_gib}GiB; free=${disk_free_gib}GiB; environment=${environment}; kernel=$(uname -sr)"
  echo "machine_specs=$machine_specs" >> "$GITHUB_OUTPUT"
  printf '### %s runner\n\n%s\n' "$deployment" "$machine_specs" >> "$GITHUB_STEP_SUMMARY"
}
