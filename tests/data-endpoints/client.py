"""
Rosetta API client with schema validation.
"""

import os
import httpx
import yaml
from pathlib import Path
from typing import Optional, Dict, Any
from jsonschema import Draft4Validator


class SchemaValidator:
    """Validates API responses against OpenAPI schemas."""

    def __init__(self, openapi_path: Optional[Path] = None, relaxed_mode: bool = False):
        """
        Initialize schema validator.

        Args:
            openapi_path: Path to OpenAPI spec file
            relaxed_mode: If True, relaxes validation for pruned instances
                         (makes address field optional in account objects)
        """
        if openapi_path is None:
            openapi_path = (
                Path(__file__).parent.parent.parent
                / "api/src/main/resources/rosetta-specifications-1.4.15/api.yaml"
            ).resolve()

        if not openapi_path.exists():
            raise FileNotFoundError(f"OpenAPI spec not found at {openapi_path}")

        with open(openapi_path, "r") as f:
            self.spec = yaml.safe_load(f)

        if "components" not in self.spec or "schemas" not in self.spec["components"]:
            raise ValueError("Invalid OpenAPI spec")

        self.schemas = self.spec["components"]["schemas"]
        self.relaxed_mode = relaxed_mode
        self.validators = {}

        # Load ALL schemas with resolved references
        for schema_name in self.schemas:
            resolved = self._resolve_refs(self.schemas[schema_name])
            # Apply pruning-specific schema relaxations
            if self.relaxed_mode:
                resolved = self._apply_pruning_relaxations(resolved, schema_name)
            self.validators[schema_name] = Draft4Validator(resolved)

    def _apply_pruning_relaxations(self, schema: dict, schema_name: str) -> dict:
        """
        Apply schema relaxations for pruned instances.

        When pruning is enabled, historical transactions may have empty account objects.
        This method makes the 'address' field optional in AccountIdentifier objects.
        """
        if schema_name in ["SearchTransactionsResponse", "BlockResponse", "BlockTransactionResponse"]:
            # These responses contain operations with account identifiers
            # We need to make address field optional in nested account objects
            schema = self._make_account_address_optional(schema)
        return schema

    def _make_account_address_optional(self, obj: Any) -> Any:
        """
        Recursively traverse schema and make address field optional in AccountIdentifier.

        This handles the case where pruned instances return empty account objects.
        """
        if isinstance(obj, dict):
            # Check if this is an AccountIdentifier schema
            if obj.get("type") == "object" and "properties" in obj:
                props = obj.get("properties", {})
                # If this object has an address property that's required
                if "address" in props and "required" in obj:
                    # Remove 'address' from required fields
                    obj["required"] = [r for r in obj["required"] if r != "address"]

            # Recursively process all nested objects
            return {k: self._make_account_address_optional(v) for k, v in obj.items()}
        elif isinstance(obj, list):
            return [self._make_account_address_optional(item) for item in obj]
        return obj

    def _resolve_refs(self, obj, depth=0):
        """Recursively resolve all $ref references."""

        if isinstance(obj, dict):
            if "$ref" in obj:
                ref_path = obj["$ref"].split("/")
                if ref_path[0] == "#":
                    resolved = self.spec
                    for part in ref_path[1:]:
                        resolved = resolved.get(part, {})
                    return self._resolve_refs(resolved, depth + 1)
            return {k: self._resolve_refs(v, depth + 1) for k, v in obj.items()}
        elif isinstance(obj, list):
            return [self._resolve_refs(item, depth + 1) for item in obj]
        return obj

    def validate_response(
        self, response_data: Any, schema_name: str
    ) -> tuple[bool, list]:
        """Validate response data against schema."""
        if schema_name not in self.validators:
            raise ValueError(f"Schema '{schema_name}' not found")

        # Draft4Validator handles ALL validation including nested objects
        errors = list(self.validators[schema_name].iter_errors(response_data))
        return len(errors) == 0, errors


class RosettaClient:
    """Rosetta API client with schema validation."""

    def __init__(
        self, base_url: str = "http://localhost:8082",
        validate_schemas: bool = True,
        relaxed_validation: bool = None
    ):
        """
        Initialize Rosetta client.

        Args:
            base_url: Base URL for Rosetta API
            validate_schemas: Whether to validate responses against OpenAPI schemas
            relaxed_validation: Enable relaxed schema validation (makes account address optional).
                              If None (default) and schema validation stays enabled, the setting is
                              derived from REMOVE_SPENT_UTXOS. Ignored when validate_schemas=False.
        """
        self.base_url = base_url
        self.client = httpx.Client(timeout=httpx.Timeout(600.0))
        self.validate_schemas = validate_schemas

        # Read pruning config from environment instead of API detection
        if relaxed_validation is None and validate_schemas:
            pruning_enabled = os.environ.get("REMOVE_SPENT_UTXOS", "false").lower() == "true"
            self.relaxed_validation = pruning_enabled
        else:
            self.relaxed_validation = relaxed_validation

        self.validator = SchemaValidator(relaxed_mode=self.relaxed_validation) if validate_schemas else None

    def _post(
        self, path: str, body: Dict[str, Any], schema_name: Optional[str] = None
    ) -> httpx.Response:
        """POST request with schema validation."""
        response = self.client.post(
            f"{self.base_url}{path}",
            json=body,
            headers={"Content-Type": "application/json"},
        )

        if self.validate_schemas and schema_name and self.validator:
            try:
                if response.status_code == 200:
                    response_data = response.json()
                    is_valid, errors = self.validator.validate_response(
                        response_data, schema_name
                    )
                    if not is_valid:
                        response.validation_errors = errors
                        error_msgs = "; ".join(str(e.message) for e in errors[:3])
                        if len(errors) > 3:
                            error_msgs += f" (and {len(errors) - 3} more)"
                        raise AssertionError(
                            f"Schema validation failed for {path}: {error_msgs}"
                        )
                else:
                    # Validate error responses against Error schema
                    error_data = response.json()
                    is_valid, errors = self.validator.validate_response(
                        error_data, "Error"
                    )
                    if not is_valid:
                        response.validation_errors = errors
            except (ValueError, KeyError):
                pass  # Not JSON or can't parse

        return response

    def search_transactions(
        self, network: Optional[str] = "preprod", **kwargs
    ) -> httpx.Response:
        """
        Search transactions endpoint.

        Parameters:
            network: Network name (preprod, mainnet, etc). Pass None to omit network_identifier.
            **kwargs: limit, offset, max_block, status, success, type,
                     account_identifier, transaction_identifier, currency, operator
        """
        if network is not None:
            body = {"network_identifier": {"blockchain": "cardano", "network": network}}
        else:
            body = {}

        # Add optional parameters
        body.update({k: v for k, v in kwargs.items() if v is not None})

        return self._post(
            "/search/transactions", body, schema_name="SearchTransactionsResponse"
        )

    def network_list(self) -> httpx.Response:
        """List supported networks."""
        return self._post("/network/list", {}, schema_name="NetworkListResponse")

    def network_status(self, network: str = "preprod") -> httpx.Response:
        """Get network status."""
        body = {"network_identifier": {"blockchain": "cardano", "network": network}}
        return self._post("/network/status", body, schema_name="NetworkStatusResponse")

    def network_options(self, network: str = "preprod") -> httpx.Response:
        """Get network options."""
        body = {"network_identifier": {"blockchain": "cardano", "network": network}}
        return self._post(
            "/network/options", body, schema_name="NetworkOptionsResponse"
        )

    def block(self, network: Optional[str] = "preprod", **kwargs) -> httpx.Response:
        """
        Get block by identifier.

        Parameters:
            network: Network name. Pass None to omit network_identifier.
            **kwargs: block_identifier (dict with 'index' and/or 'hash')
        """
        if network is not None:
            body = {"network_identifier": {"blockchain": "cardano", "network": network}}
        else:
            body = {}

        body.update({k: v for k, v in kwargs.items() if v is not None})
        return self._post("/block", body, schema_name="BlockResponse")

    def block_transaction(
        self, network: Optional[str] = "preprod", **kwargs
    ) -> httpx.Response:
        """
        Get transaction from specific block.

        Parameters:
            network: Network name. Pass None to omit network_identifier.
            **kwargs: block_identifier (requires both index AND hash),
                     transaction_identifier
        """
        if network is not None:
            body = {"network_identifier": {"blockchain": "cardano", "network": network}}
        else:
            body = {}

        body.update({k: v for k, v in kwargs.items() if v is not None})
        return self._post(
            "/block/transaction", body, schema_name="BlockTransactionResponse"
        )

    def account_balance(
        self, network: Optional[str] = "preprod", **kwargs
    ) -> httpx.Response:
        """
        Get account balance (current or historical).

        Parameters:
            network: Network name. Pass None to omit network_identifier.
            **kwargs: account_identifier (required),
                     block_identifier (optional for historical queries)
        """
        if network is not None:
            body = {"network_identifier": {"blockchain": "cardano", "network": network}}
        else:
            body = {}

        body.update({k: v for k, v in kwargs.items() if v is not None})
        return self._post(
            "/account/balance", body, schema_name="AccountBalanceResponse"
        )

    def account_coins(
        self, network: Optional[str] = "preprod", **kwargs
    ) -> httpx.Response:
        """
        Get account unspent coins (UTXOs).

        Note: Returns current unspent UTXOs only (by definition).

        Parameters:
            network: Network name. Pass None to omit network_identifier.
            **kwargs: account_identifier (required)
        """
        if network is not None:
            body = {"network_identifier": {"blockchain": "cardano", "network": network}}
        else:
            body = {}

        body.update({k: v for k, v in kwargs.items() if v is not None})
        return self._post("/account/coins", body, schema_name="AccountCoinsResponse")

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.client.close()
