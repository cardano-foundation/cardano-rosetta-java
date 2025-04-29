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

## Online Mode

In online mode, the system connects to the Cardano network, synchronizes the blockchain, and provides real-time access to blockchain data. This is the default mode and requires a connection to the Cardano network.

The online mode includes all components:

- Cardano Node
- YACI Indexer
- PostgreSQL
- Rosetta API

This mode is used for blockchain synchronization, transaction broadcasting, and querying real-time network state - typical production deployment scenario.

  </TabItem>
  <TabItem value="offline" label="Offline Mode">

## Offline Mode

In offline mode, the system operates without connecting to the Cardano network. The Cardano Node, YACI Indexer, and PostgreSQL components are disabled, and only the Rosetta API is active.

Offline mode is useful for transaction construction and signing in air-gapped environments or security-critical applications. This mode allows you to create and sign transactions without broadcasting them to the network, which is particularly important for cold wallet solutions and high-security setups.

:::tip Enabling Offline Mode
To enable offline mode, set the `API_SPRING_PROFILES_ACTIVE` environment variable to `offline` in the configuration file:

```bash
API_SPRING_PROFILES_ACTIVE=offline
```

:::

  </TabItem>
</Tabs>
