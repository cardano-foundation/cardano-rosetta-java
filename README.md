[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=cardano-foundation_cardano-rosetta-java)

## What the project is about?

This repository provides a lightweight java implementation of the [Rosetta API](https://github.com/coinbase/mesh-specifications). It uses [Yaci-Store](https://github.com/bloxbean/yaci-store) as an indexer
to fetch the data from a Cardano node. 

## :construction: Current Development status :construction:
- [x] Architecture clean up for yaci-store
- [x] Docker-compose setup 
- [x] Integration test setup
- API calls
  - Data API
    - [x] /network/*
    - [x] /block/*
    - [x] /account/*
    - /mempool
      - [x] /mempool
      - [ ] /mempool/transaction
  - [x] Construction API
- [ ] Extending Tests
  - [ ] Comparison with [Rosetta-Ts](https://github.com/cardano-foundation/cardano-rosetta)
  - [ ] Rosetta-cli test
- [ ] Refactoring

## Documentation

Please refer to our [wiki page](https://github.com/cardano-foundation/cardano-rosetta-java/wiki) for more information on the project.

---
Thanks for visiting and enjoy :heart:!
