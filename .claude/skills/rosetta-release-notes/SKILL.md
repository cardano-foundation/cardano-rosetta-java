---
name: rosetta-release-notes
description: "Generate or improve release notes for cardano-rosetta-java GitHub releases. Covers all manually-written sections: opening paragraph, Key Improvements, Upgrade / Compatibility, and API / Configuration. Use when the user asks to write, draft, update, or iterate on release notes for cardano-rosetta-java, or when comparing versions to identify API-visible changes. Triggers on keywords like 'release notes', 'api changes between versions', 'what changed in v2.x', 'draft release notes'."
---

# Rosetta Release Notes

Generate or iterate on release notes for cardano-rosetta-java by investigating git diffs between version tags.

## Modes

- **From scratch**: generate all manual sections (opening, Key Improvements, Upgrade / Compatibility, API / Configuration) from git investigation
- **Iterate on existing**: fetch current release with `gh release view <tag>`, identify gaps or writing issues in specific sections, propose targeted improvements

## Repo context

- **Repo**: `cardano-foundation/cardano-rosetta-java`
- **Rosetta endpoints**: `/block`, `/block/transaction`, `/account/balance`, `/account/coins`, `/search/transactions`, `/construction/preprocess`, `/construction/payloads`, `/construction/metadata`, `/construction/parse`, `/construction/combine`, `/construction/submit`, `/construction/hash`, `/construction/derive`, `/network/list`, `/network/status`, `/network/options`, `/mempool`, `/mempool/transaction`
- **API spec**: `api/src/main/resources/rosetta-specifications-1.4.15/api.yaml`
- **Env files**: `.env.docker-compose` (production mainnet), `.env.docker-compose-preprod` (production preprod), `.env.h2` / `.env.h2-testdata` (local dev/test), `.env.IntegrationTest` (CI). Changes in `.env.docker-compose` are production-facing and belong in release notes. Changes only in `.env.h2` or `.env.IntegrationTest` are not user-facing.
- **Error types**: `api/src/main/java/org/cardanofoundation/rosetta/common/enumeration/RosettaErrorType.java`
- **Operation types**: `api/src/main/java/org/cardanofoundation/rosetta/common/enumeration/OperationType.java`
- **Docs site**: `https://cardano-foundation.github.io/cardano-rosetta-java/docs/`

## Investigation workflow

### 1. Identify version range

Determine the two tags to compare. User provides them or infer from context (e.g., "2.1.0 release notes" → compare `2.0.0..2.1.0`). Verify tags exist with `git tag`.

### 2. Fetch existing release notes (if iterating)

Run `gh release view <new-tag>`. Note what's already written — avoid duplicating content across sections.

### 3. Investigate diffs (parallel)

Run these in parallel:

- **API spec**: `git diff <old>..<new> -- api/src/main/resources/rosetta-specifications-1.4.15/api.yaml`
- **Env files**: `git diff <old>..<new> -- .env.docker-compose .env.h2 .env.IntegrationTest`
- **Production code** (stat only): `git diff <old>..<new> --stat -- api/src/main/java/ yaci-indexer/src/main/java/`
- **Java test changes** (stat only): `git diff <old>..<new> --stat -- api/src/test/java/` (exclude load tests)
- **Python test changes** (stat only): `git diff <old>..<new> --stat -- tests/` (exclude load tests)
- **Error types**: `git diff <old>..<new> -- api/src/main/java/org/cardanofoundation/rosetta/common/enumeration/RosettaErrorType.java`
- **Operation types**: `git diff <old>..<new> -- api/src/main/java/org/cardanofoundation/rosetta/common/enumeration/OperationType.java`
- **Exception handler**: `git diff <old>..<new> -- api/src/main/java/org/cardanofoundation/rosetta/common/exception/`
- **Documentation pages**: `git diff <old>..<new> --stat -- docs/`

### 4. Deep-dive into test changes

Tests are the best clue for API-visible behavior changes. Classify each change:

- **HTTP status change** → API-visible, likely breaking
- **Response body/shape change** → API-visible
- **New error codes** → API-visible
- **New operations in responses** → API-visible
- **Test data value changes** (balances, counts, hashes) → check production code. If only test-data-generator changed, NOT an API behavior change
- **Internal refactors** (class renames, method signatures) → NOT API-visible
- **Test infrastructure** (base classes, Allure migration) → NOT API-visible

### 5. Verify every claim against diffs

Every claim in the release notes must be backed by a specific line in a diff. If you can't point to the diff line, the claim is fabricated.

**Rule 1: no diff evidence = no claim.** For every env var change, every "new" feature, every behavior change — cite the actual diff. Don't infer changes from commit messages or code structure alone.

**Rule 2: check existence in previous version.** A diff only shows what changed — it won't show things that already existed. When claiming something is "new" (new error code, new field, new operation), always verify it didn't exist in the previous tag:
```bash
git show <old-tag>:<file> | grep "PATTERN"
```
If it's already there, the change is behavioral (how it's used), not additive (new code).

**Rule 3: distinguish preparation from exposure.** A dependency enabled in the indexer (e.g., governance starter uncommented) is NOT an API change unless an endpoint actually exposes that data in this version. Check whether production controller/service code uses the new capability before claiming it as an API-visible change.

### 6. Write the sections

Follow the format and rules below for each section.

## Release page structure

The GitHub release page has these sections:

1. **Title + opening paragraph** — one-liner summary of the release
2. **Key Improvements** — bullet list of high-level features/changes
3. **Upgrade / Compatibility** — upgrade path, resync requirements, infrastructure breaking changes
4. **API / Configuration** — explicit API and env var changes with affected endpoints
5. **What's Changed** — auto-generated by GitHub (list of merged PRs)
6. **New Contributors** — auto-generated by GitHub

Sections 5 and 6 are auto-generated — never touch them. The skill covers sections 1–4.

### Detail level by section

Each section has a distinct role. Never repeat the same information across sections.

| Section | Role | Detail level |
|---|---|---|
| Title + opening | Theme of the release | One sentence, no specifics |
| Key Improvements | What changed | One line per feature, no field names, no endpoint paths, no technical values. Just the human-readable summary. |
| Upgrade / Compatibility | What the operator must do | Migration steps, resync scope, removed infra. Don't re-explain features — just say what action is needed. |
| API / Configuration | Technical precision | Endpoint paths, field names, old→new values, breaking details. This is where the specifics live. |

## Section formats and rules

### Title + opening paragraph

```markdown
# Cardano Rosetta Java vX.Y.Z

Cardano Rosetta Java vX.Y.Z introduces [concise summary of the release theme].
```

Keep it to one sentence. Don't repeat what's in Key Improvements.

### Key Improvements

```markdown
## Key Improvements
- [Feature/change description with relevant links]
- [Another feature]
```

Rules:
- **Only externally visible changes** — include only what an API consumer or deployment operator would notice. Internal config properties, dependency wiring, or build-only changes don't belong here.
- **High-level only** — one line per feature describing *what* changed in plain language. Do NOT include endpoint paths (like `/network/status`), field names (like `sync_status.stage`), specific values (like `SYNCING`, `APPLYING_INDEXES`), env var names, or technical implementation details (like `JOOQ classes`, `peer selection randomization`). Those belong in API / Configuration.
  - Good: "Sync status now reports three stages, giving operators visibility into progress and index readiness"
  - Bad: "`/network/status` now includes a `stage` field in `sync_status` with values `SYNCING`, `APPLYING_INDEXES`, and `LIVE`"
- Use consistent capitalization for domain terms: `DRep` (not `dRep`), `SPO`, `CIP-129`
- Link CIPs inline: `[CIP-129](https://cips.cardano.org/cip/CIP-0129)`
- Keep bullets self-contained — each should make sense without reading the others
- **No repetition within the section** — never mention the same feature, link, or docs page in multiple bullets. If a docs page relates to a feature, link it inline in that feature's bullet.
- **Docs pages go inline** — link new docs pages in the relevant feature bullet. Do NOT create a separate "New documentation" bullet unless the page has no parent feature to attach to.
- **Attribute benefits precisely** — if a performance gain comes from multiple changes, name them all or use neutral phrasing. Don't credit only one.

### Upgrade / Compatibility

```markdown
## Upgrade / Compatibility
- **From vX.Y.Z**: [compatible / requires resync / specific migration steps]
- **From older versions**: [compatibility notes]
- [Any removed deployment methods, changed defaults, or infrastructure breaking changes]
```

Always answer these questions:
1. Can users upgrade from the previous minor/patch version without resync?
2. Can users upgrade from older major versions?
3. Were any deployment methods or infrastructure components removed or changed (e.g., containers removed, new sidecar services required)?
4. When resync is required, what is the scope? Specify "yaci-indexer resync only (Cardano Node data can be kept)" — schema/PG changes affect the indexer database, NOT the Cardano Node blockchain data. Link to the [resync procedure](https://cardano-foundation.github.io/cardano-rosetta-java/docs/advanced-configuration/pruning#how-to-resynchronize-the-indexer).

Notes:
- Environment variable and configuration changes belong in **API / Configuration**, not here.
- Don't re-explain features already covered in Key Improvements. Focus on *what the operator needs to do*, not *what the feature is*.

### API / Configuration

Uses bullet points with bold title + indented detail.

#### Ordering rules
1. Breaking API behavior changes first (bold "Breaking:" prefix) — reserve "Breaking:" for changes to endpoint responses, status codes, or request formats that require client code updates. Env var renames/removals are NOT "Breaking:" items — they go in the environment updates list.
2. Endpoint-specific changes next (group by endpoint path)
3. Environment variable updates last — grouped under a single `**Environment updates:**` header as a flat list, NOT as individual bold-title bullets. Use `old value → new value` format.

#### Content rules
- Each item is a bullet with **bold title**, detail indented below
- Every change must name the affected endpoints explicitly
- Cross-cutting changes must list ALL affected endpoints in parentheses
- **Only API behavior and env var changes** — internal library version bumps (pom.xml dependencies like Yaci Store, client-lib) don't belong here unless exposed as user-facing env vars. Internal preparations (e.g., governance starter enabled but no endpoint serving the data) are not API changes.
- Add inline links to project docs and external specs where relevant:
  - DRep-related: [DRep Delegation guide](https://cardano-foundation.github.io/cardano-rosetta-java/docs/user-guides/drep-delegation)
  - Pool-related: [Pool Operations guide](https://cardano-foundation.github.io/cardano-rosetta-java/docs/user-guides/pool-operations)
  - Staking-related: [Staking guide](https://cardano-foundation.github.io/cardano-rosetta-java/docs/user-guides/staking)
  - CIP references: `https://cips.cardano.org/cip/CIP-XXXX`
  - Conway/Voltaire: [Chang upgrade](https://docs.cardano.org/about-cardano/evolution/upgrades/chang/)
- Do NOT link to generic external references unless truly necessary
- Environment updates: group under a single `**Environment updates:**` header as a flat bullet list. Show `old value → new value`. For removed vars, show the old value and note "(removed)". For new vars, show the value and note "(new)".

## Writing rules

### Evidence rules
- **No diff evidence = no claim** — every change asserted must map to a specific line in a git diff
- **"New" means absent in previous version** — verify with `git show <old-tag>:<file> | grep "PATTERN"`
- **Value changes require diff proof** — cite both old and new lines from the diff
- **Tests are clues, not proof** — always cross-check with `src/main/java` changes
- **Test data regeneration is not a bug fix** — if only `test-data-generator/` changed, endpoint behavior is unchanged

### Visibility rules
- **Only externally visible changes** — API consumers and deployment operators. Internal config, dependency wiring, or build changes don't qualify.
- **Preparation ≠ exposure** — enabling a dependency is NOT an API change unless endpoint code actually serves that data in this version
- **Scope matters** — `.env.docker-compose` = production. Changes only in `.env.h2` or `.env.IntegrationTest` are not user-facing.

## Examples

See [references/examples.md](references/examples.md) for complete v2.0.0 and v2.1.0 examples.
