---
sidebar_position: 2
title: Performance Measurements
description: Performance measurement methodologies and results
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

import MidDockerCompose132 from './test-results/1.3.2/mid_profile/docker-compose.md';

import AdvDockerCompose131_pruned  from './test-results/1.3.1/advanced_profile/docker-compose-pruned.md';
import MidDockerCompose131 from './test-results/1.3.1/mid_profile/docker-compose.md';

import MidDockerCompose129 from './test-results/1.2.9/mid_profile/docker-compose.md';
import MidHugeAddress373kv129 from './test-results/1.2.9/mid_profile/huge-address-373k.md';
import MidHugeAddress16Mv129 from './test-results/1.2.9/mid_profile/huge-address-1.6M.md';

import MidDockerCompose129_pruned from './test-results/1.2.9/mid_profile/docker-compose-pruned.md';
import MidHugeAddress373kv129_pruned from './test-results/1.2.9/mid_profile/huge-address-373k-pruned.md';
import MidHugeAddress16Mv129_pruned from './test-results/1.2.9/mid_profile/huge-address-1.6M-pruned.md';

import AdvDockerCompose129  from './test-results/1.2.9/advanced_profile/docker-compose.md';
import AdvHugeAddress373kv129  from './test-results/1.2.9/advanced_profile/huge-address-373k.md';
import AdvHugeAddress16Mv129  from './test-results/1.2.9/advanced_profile/huge-address-1.6M.md';

import AdvDockerCompose129_pruned  from './test-results/1.2.9/advanced_profile/docker-compose-pruned.md';
import AdvHugeAddress373kv129_pruned  from './test-results/1.2.9/advanced_profile/huge-address-373k-pruned.md';
import AdvHugeAddress16Mv129_pruned   from './test-results/1.2.9/advanced_profile/huge-address-1.6M-pruned.md';


// import SingleDocker127 from './test-results/1.2.7/single-docker.md';
import DockerCompose127 from './test-results/1.2.7/docker-compose.md';

// import SingleDocker126 from './test-results/1.2.6/single-docker.md';
import DockerCompose126 from './test-results/1.2.6/docker-compose.md';

import DockerCompose120 from './test-results/1.2.0/docker-compose.md';
import HugeAddress373kv120 from './test-results/1.2.0/huge-address-373k.md';

# Performance Measurements

This page documents load test results for different releases of `cardano-rosetta-java`, evaluating performance across various deployment scenarios.

### Service Level Agreements (SLAs)

Our performance evaluation is based on the following Service Level Agreements (SLAs), which define the acceptable thresholds for production environments:

- **Response Time**: A **p99 of less than 1 second** is the target for all standard API endpoints.
- **Error Rate**: A **non-2xx response rate of less than 1%** is considered acceptable.

In specific high-load scenarios, such as querying addresses with hundreds of thousands of transactions, these SLAs may be adjusted to assess system stability under extreme conditions. Any such adjustments are noted in the relevant test results.

:::note
Load tests are conducted using Apache Bench (ab) with a ramp-up strategy, progressively increasing concurrency to a predefined ceiling of 500 simultaneous requests.
:::

:::tip
To better understand the environments in which these results were obtained, please refer to our [hardware profiles documentation](../install-and-deploy/hardware-profiles).
:::

<details>
<summary>
### v1.3.2 (Sep 5, 2025)
</summary>
- [Release Notes](https://github.com/cardano-foundation/cardano-rosetta-java/releases/tag/1.3.2)

<details>
<summary>
 **Mid-level Hardware Profile** 
</summary>
**Machine Specs:** 8 cores, 8 threads, 47GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+
<details>
<Tabs>
  <TabItem value="mid_docker_compose132" label="Docker Compose" default>
    <MidDockerCompose132 />
  </TabItem>
</Tabs>
</details>
</details>
</details>

<details>
<summary>
### v1.3.1 (Aug 7, 2025)
</summary>
- [Release Notes](https://github.com/cardano-foundation/cardano-rosetta-java/releases/tag/1.3.1)
<details>
<summary>
 **Mid-level Hardware Profile** 
</summary>
**Machine Specs:** 8 cores, 8 threads, 47GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

Maximum concurrency achieved for each modes:
<details>

<summary>
#### Pruning Disabled (`REMOVE_SPENT_UTXOS=false`)
</summary>
<Tabs>
  <TabItem value="mid_docker_compose131" label="Docker Compose" default>
    <MidDockerCompose131 />
  </TabItem>
</Tabs>
</details>
</details>

<details>
<summary>
 **Advanced-level Hardware Profile** 
</summary>
**Machine Specs:** 16 cores, 16 threads, 47GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

Maximum concurrency achieved for each modes:
<details>
<summary>
#### Pruning Enabled (`REMOVE_SPENT_UTXOS=true`)
</summary>
<Tabs>
  <TabItem value="adv_docker_compose131_pruned" label="Docker Compose" default>
    <AdvDockerCompose131_pruned />
  </TabItem>
</Tabs>
</details>
</details>
</details>



<details>
<summary>
### v1.2.9 (Jun 11, 2025)
</summary>
- [Release Notes](https://github.com/cardano-foundation/cardano-rosetta-java/releases/tag/1.2.9)

<details>
<summary>
 **Mid-level Hardware Profile** 
</summary>
**Machine Specs:** 8 cores, 8 threads, 47GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

Maximum concurrency achieved for each modes:
<details>
<summary>
#### Pruning Enabled (`REMOVE_SPENT_UTXOS=true`)
</summary>
<Tabs>
  <TabItem value="compose" label="Docker Compose" default>
    <MidDockerCompose129_pruned />
  </TabItem>
  <TabItem value="huge-373k-v129" label="Huge Address (~373k txs)">
    <MidHugeAddress373kv129_pruned />
  </TabItem>
  <TabItem value="huge-1.6M-v129" label="Huge Address (~1.6M txs)">
    <MidHugeAddress16Mv129_pruned />
  </TabItem>
</Tabs>
</details>

<details>
<summary>
#### Pruning Disabled (`REMOVE_SPENT_UTXOS=false`)
</summary>
<Tabs>
  <TabItem value="adv_docker_compose129" label="Docker Compose" default>
    <MidDockerCompose129 />
  </TabItem>
  <TabItem value="adv_docker_compose129_huge-373k-v129" label="Huge Address (~373k txs)">
    <MidHugeAddress373kv129 />
  </TabItem>
  <TabItem value="adv_docker_compose129_huge-1.6M-v129" label="Huge Address (~1.6M txs)">
    <MidHugeAddress16Mv129 />
  </TabItem>
</Tabs>
</details>
</details>

<details>
<summary>
 **Advanced-level Hardware Profile** 
</summary>
**Machine Specs:** 16 cores, 16 threads, 47GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

Maximum concurrency achieved for each modes:
<details>
<summary>
#### Pruning Enabled (`REMOVE_SPENT_UTXOS=true`)
</summary>
<Tabs>
  <TabItem value="compose" label="Docker Compose" default>
    <AdvDockerCompose129_pruned />
  </TabItem>
  <TabItem value="huge-373k-v129" label="Huge Address (~373k txs)">
    <AdvHugeAddress373kv129_pruned />
  </TabItem>
  <TabItem value="huge-1.6M-v129" label="Huge Address (~1.6M txs)">
    <AdvHugeAddress16Mv129_pruned />
  </TabItem>
</Tabs>
</details>

<details>
<summary>
#### Pruning Disabled (`REMOVE_SPENT_UTXOS=false`)
</summary>
<Tabs>
  <TabItem value="adv_docker_compose129" label="Docker Compose" default>
    <AdvDockerCompose129 />
  </TabItem>
  <TabItem value="adv_docker_compose129_huge-373k-v129" label="Huge Address (~373k txs)">
    <AdvHugeAddress373kv129 />
  </TabItem>
  <TabItem value="adv_docker_compose129_huge-1.6M-v129" label="Huge Address (~1.6M txs)">
    <AdvHugeAddress16Mv129 />
  </TabItem>
</Tabs>
</details>
</details>

</details>


<details>
<summary>
### v1.2.7 (Apr 29, 2025)
</summary>

- [Release Notes](https://github.com/cardano-foundation/cardano-rosetta-java/releases/tag/1.2.7)

<Tabs>
  <!-- <TabItem value="single" label="Single Docker" default>
    <SingleDocker127 />
  </TabItem> -->
  <TabItem value="compose" label="Docker Compose">
    <DockerCompose127 />
  </TabItem>
</Tabs>
</details>

<details>
<summary>

### v1.2.6 (Apr 15, 2025)

</summary>


- [Release Notes](https://github.com/cardano-foundation/cardano-rosetta-java/releases/tag/1.2.6)

<Tabs>
  <TabItem value="single" label="Single Docker" default>
    <SingleDocker126 />
  </TabItem>
  <TabItem value="compose" label="Docker Compose">
    <DockerCompose126 />
  </TabItem>
</Tabs>
</details>

<details>
<summary>

### v1.2.0 (Feb 13, 2025)

</summary>

- [Release Notes](https://github.com/cardano-foundation/cardano-rosetta-java/releases/tag/1.2.0)

The following tests were conducted on a **mid-level** hardware profile with the following specifications: **8 cores, 8 threads, 48GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+**.

<Tabs>
  <TabItem value="compose" label="Docker Compose">
    <DockerCompose120 />
  </TabItem>
  <TabItem value="huge-373k-v120" label="Huge Address (~373k txs)">
    <HugeAddress373kv120 />
  </TabItem>
</Tabs>
</details>
