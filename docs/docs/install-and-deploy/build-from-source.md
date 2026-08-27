---
sidebar_position: 5
title: Building from Source
description: Build Docker images locally and import them into K3s for Helm deployments
---

# Building from Source

Build Docker images locally from the repository using Docker Compose. For Docker Compose deployments, the images are used directly. For Kubernetes (K3s) deployments, the images need to be imported into containerd.

## Prerequisites

- Docker with Compose plugin
- Clone of `cardano-rosetta-java` checked out to the target branch or tag
- A valid `.env` file (see [Environment Variables](./env-vars) for details)

## Build all images

```bash
cd cardano-rosetta-java
docker compose build
```

This builds all service images defined in the compose files. Image tags are derived from your `.env`:

| Image | Tag source |
|---|---|
| `cardanofoundation/cardano-rosetta-java-api` | `RELEASE_VERSION` |
| `cardanofoundation/cardano-rosetta-java-indexer` | `RELEASE_VERSION` |
| `cardanofoundation/cardano-rosetta-java-cardano-node` | `CARDANO_NODE_VERSION` |
| `cardanofoundation/cardano-rosetta-java-postgres` | `PG_VERSION_TAG` |
| `cardanofoundation/cardano-rosetta-java-mithril` | `MITHRIL_VERSION` |

To build a single service:

```bash
docker compose build api
docker compose build cardano-node
```

## Import images into K3s

K3s uses containerd, not Docker. Images built with Docker need to be exported and imported before they can be used by Helm:

```bash
docker save \
  cardanofoundation/cardano-rosetta-java-api:<RELEASE_VERSION> \
  cardanofoundation/cardano-rosetta-java-indexer:<RELEASE_VERSION> \
  cardanofoundation/cardano-rosetta-java-cardano-node:<CARDANO_NODE_VERSION> \
  cardanofoundation/cardano-rosetta-java-postgres:<PG_VERSION_TAG> \
  cardanofoundation/cardano-rosetta-java-mithril:<MITHRIL_VERSION> \
  | sudo k3s ctr images import -
```

The default `imagePullPolicy` is `IfNotPresent`, so K3s uses the locally imported images instead of pulling from the registry.

:::warning Replacing images with the same tag

If you rebuild an image and import it with a tag that already exists in K3s, the old cached image is kept. You must remove the old image first:

```bash
sudo k3s ctr images rm docker.io/cardanofoundation/cardano-rosetta-java-api:<TAG>
```

Then import the new image. You can verify the correct image is loaded by checking the SHA:

```bash
sudo k3s ctr images ls | grep cardano-rosetta-java-api
```

:::

## Next steps

- [Deploy with Docker Compose](./docker)
- [Deploy with Kubernetes (K3s)](./kubernetes/deployment)
