# Release Notes Examples

## v2.1.0

```markdown
# Cardano Rosetta Java v2.1.0

Cardano Rosetta Java v2.1.0 introduces full support for 2 Conway-era governance operations across both construction and data endpoints.

## Key Improvements
- Full support for 2 governance operations (Voltaire / Conway era): SPO Voting and DRep Vote Delegation, now available in both construction and data endpoints
- Operations are now sorted by index (ascending order)
- Full support for [CIP-129](https://cips.cardano.org/cip/CIP-0129) (automatic DRep type inference from prefixed IDs)
- Added experimental admin UI for the indexer at [http://yaci-indexer:9095/admin-ui/](http://yaci-indexer:9095/admin-ui/)

## Upgrade / Compatibility
- **From v2.0.0**: Compatible, no resync required.
- **From v1.x.x**: Requires full genesis resync of the yaci-indexer only (Cardano Node data can be kept). See the [data volume removal procedure](https://cardano-foundation.github.io/cardano-rosetta-java/docs/advanced-configuration/pruning#how-to-resynchronize-the-indexer) for steps.

## API / Configuration

- **Breaking: HTTP status codes aligned with error classification.**
	- All non-retriable errors now return `400 Bad Request` instead of `500 Internal Server Error` across all endpoints. Clients relying on `500` for validation or not-found errors must update their error handling.

- **`/construction/preprocess` and `/construction/payloads`: [CIP-129](https://cips.cardano.org/cip/CIP-0129) DRep type inference.**
	- DRep IDs with a 29-byte CIP-129 prefix no longer require an explicit `type` field, the type is inferred from the header byte. Raw 28-byte IDs still require `type`. See the [DRep Delegation guide](https://cardano-foundation.github.io/cardano-rosetta-java/docs/user-guides/drep-delegation) for usage examples.

- **`/block`, `/block/transaction`, and `/search/transactions`: governance operations included.**
	- Responses now contain `VOTE_DREP_DELEGATION` and `POOL_GOVERNANCE_VOTE` operations for [Conway-era](https://docs.cardano.org/about-cardano/evolution/upgrades/chang/) transactions. See the [Pool Operations guide](https://cardano-foundation.github.io/cardano-rosetta-java/docs/user-guides/pool-operations) for SPO voting details.

**Environment updates:**
- `CARDANO_NODE_VERSION`: 10.5.3 → 10.5.4
- `YACI_VERSION`: 0.10.5 → 0.10.6
```

## v2.0.0

```markdown
# Cardano Rosetta Java v2.0.0

Cardano Rosetta Java v2.0.0 introduces major infrastructure upgrades, a unified schema, and a cleaner architecture, establishing a strong foundation for upcoming features. This release also delivers significant performance and reliability improvements, reducing sync time from ~52 hours to ~37 hours.

## Key Improvements
- Unified database schema replaces per-network schemas, simplifying deployment and maintenance
- Deferred index creation speeds up initial sync by applying database indexes only after ingestion completes. See the [Boot Sequence](https://cardano-foundation.github.io/cardano-rosetta-java/docs/install-and-deploy/boot-sequence) and [Index Management](https://cardano-foundation.github.io/cardano-rosetta-java/docs/advanced-configuration/index-management) documentation.
- Sync status now reports three stages, giving operators visibility into sync progress and index readiness
- PostgreSQL upgraded from v14 to v18
- Mithril upgraded from 2537.0 to 2543.1-hotfix
- Yaci Store upgraded from 0.1.5 to 2.0.0, contributing to reduced sync times
- Single Docker image deployment removed; Docker Compose is now the only supported method

## Upgrade / Compatibility
- **From v1.4.3 and older versions**: a full resync of yaci-indexer is required (Cardano Node data can be kept). The database schema has changed from network-specific (e.g., `mainnet`) to `public`, and PostgreSQL was upgraded from 14 to 18, existing databases are not compatible. See the [resync procedure](https://cardano-foundation.github.io/cardano-rosetta-java/docs/advanced-configuration/pruning#how-to-resynchronize-the-indexer).
- The single Docker image (`docker/Dockerfile`) has been removed. All deployments should use Docker Compose (or equivalent orchestration).
- A new `index-applier` one-shot container is included in the Docker Compose stack. It runs automatically after sync completes and requires no manual intervention.
- Default `DB_PATH` and `CARDANO_NODE_DIR` now use absolute paths under `/opt/cardano-rosetta-java/`. Existing volume mounts must be updated.

## API / Configuration

- **`/network/status`: new `stage` field in `sync_status`.**
	- The `sync_status.stage` field is now set to one of three values: `SYNCING` (catching up to tip), `APPLYING_INDEXES` (tip reached, indexes being created), or `LIVE` (fully operational). The `synced` boolean remains `true` only at `LIVE`. Clients that only check `synced` are unaffected.

**Environment updates:**
- `PG_VERSION_TAG`: `REL_14_11` → `REL_18_0`
- `DB_SCHEMA`: `${NETWORK}` → `public`
- `DB_PATH`: `data` → `/opt/cardano-rosetta-java/mainnet/sql_data`
- `CARDANO_NODE_DIR`: `/node` → `/opt/cardano-rosetta-java/mainnet/node_data`
- `MITHRIL_VERSION`: `2537.0` → `2543.1-hotfix`
- `CARDANO_CONFIG_CONTAINER_PATH`: `/config` (new)
- `RELEASE_VERSION`: `2.0.0` (new, replaces `API_DOCKER_IMAGE_TAG` and `INDEXER_DOCKER_IMAGE_TAG`)
- `API_DOCKER_IMAGE_TAG` (removed, replaced by `RELEASE_VERSION`)
- `INDEXER_DOCKER_IMAGE_TAG` (removed, replaced by `RELEASE_VERSION`)
- `ROSETTA_VERSION` (removed)
```
