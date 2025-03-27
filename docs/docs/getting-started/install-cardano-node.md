---
sidebar_position: 4
title: Install Cardano Node
description: Guide to installing and configuring Cardano Node for use with Rosetta
---

## Restore a snapshot

**TBD for yaci-store**

A node snapshot can be downloaded from [here](https://csnapshots.io/). Download the snapshot and place the files within the `CARDANO_NODE_DB` Path.

For mainnet the following command can be used to restore the snapshot:

```bash
curl -o - https://downloads.csnapshots.io/mainnet/$(curl -s https://downloads.csnapshots.io/mainnet/mainnet-db-snapshot.json| jq -r .[].file_name ) | lz4 -c -d - | tar -x -C ${CARDANO_NODE_DB}
```
