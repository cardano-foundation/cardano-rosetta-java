---
sidebar_position: 1
title: Release Quality Assurance
description: Testing methodology and procedures for releases
---

# Release Quality Assurance Process

The following steps are performed as part of the QA process for new releases:

1. **E2E Testing**: Run the complete end-to-end test flow on preprod network using the [`e2e_tests`](https://github.com/cardano-foundation/cardano-rosetta-java/tree/main/e2e_tests) suite

   - Test on both single docker and docker-compose deployments
   - Verify all construction API flows work correctly

2. **Stability Testing**: Run stability tests on mainnet using the [`load-tests`](https://github.com/cardano-foundation/cardano-rosetta-java/tree/main/load-tests) suite

   - Test on both single docker and docker-compose deployments
   - Verify performance and scalability remain acceptable for all online endpoints
   - Focus on `/data/*` endpoints, `/construction/metadata`, and `/search` endpoints

3. **Documentation**: Document test results in [Performance Measurements](./performance-measurements.md)
   - Compare results with previous versions to identify any performance regressions
