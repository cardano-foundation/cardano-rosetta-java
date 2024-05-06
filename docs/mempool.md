- [Architecture Overview](https://github.com/cardano-foundation/cardano-rosetta-java/wiki)
- [Cardano specific API Additions](./docs/cardano-specific-api-additions.md)
- User guides
    - TODO
- Development Guides
    - [Getting Started with H2](./docs/dev-h2-quick-start-guide.md)
    - [Getting Started with Docker](./docs/docker-getting-started-guide)
- [Environment Variables](./docs/environment-variables.md)
- [Contributing Guidelines](./CONTRIBUTING.md)


### Restore a snapshot
**TBD for yaci-store**

A node snapshot can be downloaded from [here](https://csnapshots.io/). Download the snapshot and place the files within the `CARDANO_NODE_DB` Path.

For mainnet the following command can be used to restore the snapshot:
```bash
curl -o - https://downloads.csnapshots.io/mainnet/$(curl -s https://downloads.csnapshots.io/mainnet/mainnet-db-snapshot.json| jq -r .[].file_name ) | lz4 -c -d - | tar -x -C ${CARDANO_NODE_DB}
```

### Mempool Monitoring
Mempool monitoring can be activated when adding the spring profile `mempool` to the rosetta api service.
It will be turned off by default. Since it is only working for nodes, which participate in the network and available within P2P from other nodes.
So the Node itself needs extra configuration to be able to query mempool transactions.
