---
sidebar_position: 2
title: Performance Measurements
description: Performance measurement methodologies and results
---

# Performance Measurements

This page documents the performance measurements and load test results for different releases of `cardano-rosetta-java`.

## Load Test Results

Below you can find the load test results for various releases, comparing different deployment options. All performance tests are evaluated against a Service Level Agreement (SLA) target of 1 second response time, which we consider the acceptable threshold for API responsiveness in production environments.

<details>
<summary>

### Release 1.2.7 (Test Run: 2025-04-16)

</summary>

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import SingleDocker from './\_test-results/1.2.6-dev_2025-04-16/single-docker.md';
import DockerCompose from './\_test-results/1.2.6-dev_2025-04-16/docker-compose.md';

<Tabs>
  <TabItem value="single" label="Single Docker" default>
    <SingleDocker />
  </TabItem>
  <TabItem value="compose" label="Docker Compose">
    <DockerCompose />
  </TabItem>
</Tabs>
</details>

---

_Future results will be added here, each as a collapsible section._
