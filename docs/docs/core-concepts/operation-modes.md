---
sidebar_label: Operation Modes
sidebar_position: 4
---

# Operation Modes

Cardano Rosetta Java can operate in two primary modes: online and offline.

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="online" label="Online Mode" default>



In online mode, the system connects to the Cardano network, synchronizes the blockchain, and provides real-time access to blockchain data. This is the default mode and requires a connection to the Cardano network.

The online mode includes all components:

- Cardano Node
- YACI Indexer
- PostgreSQL
- Rosetta API

This mode is used for blockchain synchronization, transaction broadcasting, and querying real-time network state - typical production deployment scenario.

  </TabItem>
  <TabItem value="offline" label="Offline Mode">



In offline mode, the system operates without connecting to the Cardano network. The Cardano Node, YACI Indexer, and PostgreSQL components are disabled, and only the Rosetta API is active.

Offline mode is useful for transaction construction and signing in air-gapped environments or security-critical applications. This mode allows you to create and sign transactions without broadcasting them to the network, which is particularly important for cold wallet solutions and high-security setups.

:::tip Enabling Offline Mode
To enable offline mode, set the `API_SPRING_PROFILES_ACTIVE` and `TOKEN_REGISTRY_ENABLED` as below:
```bash
API_SPRING_PROFILES_ACTIVE=offline
TOKEN_REGISTRY_ENABLED=false
```
:::

#### Deployment Option

##### Using Docker Compose

1. Clone the repository
```bash
git clone https://github.com/cardano-foundation/cardano-rosetta-java.git
```
2. Use the provided environment files:
   - The default configuration is in `.env.docker-compose`

```bash
docker compose --env-file .env.docker-compose \
  -f docker-compose-offline.yaml up -d
```

  </TabItem>
</Tabs>
