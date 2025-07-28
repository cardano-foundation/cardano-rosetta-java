# Handling Unparsed Blocks

## 1. Overview

In some cases, **yaci-core** may not be able to parse certain Cardano blocks due to unexpected or malformed data. To address this, **yaci-store** has introduced a feature that allows these **unparsed blocks to be ignored** and **stored in a separate error table** in the database, instead of halting the indexing process.

With this graceful error handling in place, it's now possible to surface these parse-error blocks to exchanges and integrators so they can:

- Detect if any of their transactions are present in an errored block.
- Review and “mark checked” blocks if they do not impact their users or operations.

This capability is exposed as **Cardano network-specific procedure calls**, using **Coinbase Mesh’s generic `/call` endpoint**, which supports **dynamic method routing**. This allows clients to query and update the state of errored blocks in a flexible and standardized way.

Starting from **Rosetta-java version 1.3.0**, the parameter `CONTINUE_PARSING_ON_ERROR=true|false` has been introduced:

- When it is set to `true`, yaci-core will skip unparsed blocks and add them to the error table.
- When it is set to `false`, yaci-core will stop indexing when it encounters an unparsed block.

---

## 2. How to Use

You can interact with the parse-error block system through the `/call` endpoint. Below are the available operations.

### 2.1 Review blocks that could not be fully parsed

Use this call to get the list of blocks that failed to parse.

#### Request

```bash
curl --location 'http://localhost:8082/call' \
--header 'Content-Type: application/json' \
--data '{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "preprod"
  },
  "method": "get_parse_error_blocks",
  "parameters": {
    "status": "UNREVIEWED"
  }
}'
```
:::note
- Pagination is not required, as the number of errored blocks is expected to be low.
:::
#### Response
```json
{
    "result": {
        "parse_error_blocks": [
            {
                "error_id": 1,
                "block_number": 3686994,
                "status": "UNREVIEWED",
                "lastUpdated": "2025-07-18T16:18:00.266457",
                "note": "Please review all transactions within a block, https://explorer.cardano.org/block/3686994"
            },
            {
                "error_id": 2,
                "block_number": 3686995,
                "status": "UNREVIEWED",
                "lastUpdated": "2025-07-18T16:18:00.266457",
                "note": "Please review all transactions within a block, https://explorer.cardano.org/block/3686995"
            } 
        ]
    },
    "idempotent": false
}
```

### 2.2 Mark blocks as “checked” if they do not affect users or transactions
After reviewing a block, you can mark it as reviewed with a comment and status.
#### Request

```bash
curl --location 'http://localhost:8082/call' \
--header 'Content-Type: application/json' \
--data '{
  "network_identifier": {
    "blockchain": "cardano",
    "network": "preprod"
  },
  "method": "mark_parse_error_block_checked",
  "parameters": {
    "block_number": 3686994,
    "status": "REVIEWED_DOES_NOT_AFFECT_US",
    "comment": "no impact to operation",
    "checked_by": "Admin"
  }
}
```
:::note
- `block_number` specifies which block to update.
- `status` reflects the outcome of your review, e.g., REVIEWED_DOES_NOT_AFFECT_US.
- Use `comment` and `checked_by` fields to provide audit context.
:::

#### Response
```json
{
    "result": {
        "parse_error_blocks_response": [
            {
                "error_id": 1,
                "block_number": 3686994,
                "status": "REVIEWED_DOES_NOT_AFFECT_US",
                "comment": "no impact to operation",
                "checked_by": "Admin",
                "lastUpdated": "2025-07-28T04:25:57.632987219"
            }
        ]
    },
    "idempotent": true
}
```

