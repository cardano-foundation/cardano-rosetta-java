#!/bin/bash
echo "Network: $NETWORK"
if [ "$NETWORK" == "mainnet" ]; then
    NETWORK_STR="--mainnet"
    HARDFORK_EPOCH=208
else
    NETWORK_STR="--testnet-magic $PROTOCOL_MAGIC"
    HARDFORK_EPOCH=1
fi

show_progress() {
    message="$1"; percent="$2"
    done=$(bc <<< "scale=0; 40 * ${percent%.*} / 100" )
    todo=$(bc <<< "scale=0; 40 - $done" )
    done_sub_bar=$(printf "%${done}s" | tr " " "#")
    todo_sub_bar=$(printf "%${todo}s" | tr " " "-")
    echo -ne "$message [${done_sub_bar}${todo_sub_bar}] ${percent}%"\\r
}

node_synchronization() {
    echo -e "Starting Cardano node synchronization..."
    epoch_length=$(jq -r .epochLength $GENESIS_SHELLEY_PATH)
    slot_length=$(jq -r .slotLength $GENESIS_SHELLEY_PATH)
    byron_slot_length=$(( $(jq -r .blockVersionData.slotDuration $GENESIS_BYRON_PATH) / 1000 ))
    byron_epoch_length=$(( $(jq -r .protocolConsts.k $GENESIS_BYRON_PATH) * 10 ))
    byron_start=$(jq -r .startTime $GENESIS_BYRON_PATH)
    byron_end=$((byron_start + $HARDFORK_EPOCH * byron_epoch_length * byron_slot_length))
    byron_slots=$(($HARDFORK_EPOCH * byron_epoch_length))
    now=$(date +'%s')
    expected_slot=$((byron_slots + (now - byron_end) / slot_length))

    sync_progress=0
    while (( ${sync_progress%.*} < 100 )); do
        # Try to get valid tip data
        while true; do
            current_status=$(cardano-cli query tip $NETWORK_STR 2>/dev/null)
            if [ $? -eq 0 ] && [ -n "$current_status" ]; then
                current_slot=$(echo "$current_status" | jq -r '.slot')
                sync_progress=$(echo "$current_status" | jq -r '.syncProgress')
                # Only break if sync_progress is a number
                if [[ "$sync_progress" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
                    break
                fi
            fi
            echo "Waiting for cardano-cli to return valid tip data..."
            sleep 2
        done

        show_progress "Node synchronization: Slot $current_slot/$expected_slot" $sync_progress
        sleep 1
    done
    echo "Node synchronization: DONE"
}


if [ "${SYNC}" == "true" ] ; then
    node_synchronization
fi

exit 0