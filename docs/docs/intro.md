---
sidebar_position: 1
title: Overview
description: Introduction to Cardano Rosetta Java implementation
---

# Introduction

Cardano Rosetta Java is a lightweight Java implementation of the [Coinbase Mesh API](https://github.com/coinbase/mesh-specifications) (formerly Rosetta) for the Cardano blockchain. This implementation follows the [Mesh API specification](https://docs.cdp.coinbase.com/mesh/docs/api-reference/) and is compatible with the [Mesh CLI](https://docs.cdp.coinbase.com/mesh/docs/mesh-cli/), while including specific extensions to accommodate Cardano's unique features.

## Architecture

The Cardano Rosetta Java implementation consists of four key components:

- **Cardano Node**: A full Cardano node processing and validating blockchain data
- **Cardano Submit API**: An API for transaction submission to the Cardano network
- **Indexer**: A [Yaci-Store](https://github.com/bloxbean/yaci-store) based indexer that stores blockchain data in PostgreSQL
- **Rosetta API**: The implementation of the Mesh API specification

For a more detailed architecture overview, see the [Architecture](core-concepts/architecture) page.

## System Requirements

Running Cardano Rosetta Java requires the following minimum resources:

| Component   | Requirement | Notes                    |
| :---------- | :---------- | :----------------------- |
| **CPU**     | 4 Cores     |                          |
| **RAM**     | 32 GB       |                          |
| **Storage** | 1 TB        | When pruning is disabled |
|             | 400 GB      | When pruning is enabled  |

:::tip Performance Note
Better hardware resources (more CPU cores, RAM) will improve indexer and node performance, resulting in faster chain synchronization and better API response times.
:::

## Getting Started

Whether you want to run, integrate, or contribute, select a path below:

<div className="container" style={{marginTop: '2rem'}}>
  <div className="row">
    <div className="col col--4" style={{marginBottom: '2rem'}}>
      <div className="card choice-card">
        <div className="card__header">
          <h3>🚀 Run</h3>
        </div>
        <div className="card__body">
          <p>Get Cardano Rosetta Java running quickly with Docker. The fastest path for operators needing an instance.</p>
        </div>
        <div className="card__footer">
          <a href="./install-and-deploy/docker" className="button button--secondary button--block">Go to Quick Start →</a>
        </div>
      </div>
    </div>
    <div className="col col--4" style={{marginBottom: '2rem'}}>
      <div className="card choice-card">
        <div className="card__header">
          <h3>🧩 Integrate</h3>
        </div>
        <div className="card__body">
          <p>Explore architecture, Cardano specifics, and configuration guides. For developers integrating Rosetta.</p>
        </div>
        <div className="card__footer">
          <a href="./core-concepts/architecture" className="button button--secondary button--block">Explore Concepts →</a>
        </div>
      </div>
    </div>
    <div className="col col--4" style={{marginBottom: '2rem'}}>
      <div className="card choice-card">
        <div className="card__header">
          <h3>🤝 Contribute</h3>
        </div>
        <div className="card__body">
          <p>Set up your development environment and learn how to contribute code or documentation to the project.</p>
        </div>
        <div className="card__footer">
          <a href="./contributing" className="button button--secondary button--block">Start Contributing →</a>
        </div>
      </div>
    </div>
  </div>
</div>
