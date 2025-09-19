"""
Rosetta API client with schema validation.
"""

import httpx
import yaml
import json
from pathlib import Path
from typing import Optional, Dict, Any
from jsonschema import Draft4Validator


class SchemaValidator:
    """OpenAPI schema validator for responses."""

    def __init__(self, openapi_path: Optional[Path] = None):
        self.schemas = {}
        self.validators = {}

        if openapi_path is None:
            # Default path to api.yaml
            openapi_path = (
                Path(__file__).parent.parent.parent
                / "api/src/main/resources/rosetta-specifications-1.4.15/api.yaml"
            ).resolve()

        if openapi_path.exists():
            self._load_schemas(openapi_path)

    def _load_schemas(self, openapi_path: Path):
        """Load schemas from OpenAPI spec."""
        with open(openapi_path, 'r') as f:
            spec = yaml.safe_load(f)

        # Extract schemas from components
        if 'components' in spec and 'schemas' in spec['components']:
            self.schemas = spec['components']['schemas']

            # Create validators for common response schemas
            response_schemas = [
                'SearchTransactionsResponse',
                'Error',
                'Transaction',
                'Operation',
                'AccountIdentifier',
                'NetworkIdentifier'
            ]

            for schema_name in response_schemas:
                if schema_name in self.schemas:
                    # Resolve references and create validator
                    schema = self._resolve_schema(schema_name, spec)
                    self.validators[schema_name] = Draft4Validator(schema)

    def _resolve_schema(self, schema_name: str, spec: dict) -> dict:
        """Resolve $ref references in schema."""
        schema = spec['components']['schemas'].get(schema_name, {})

        def resolve_refs(obj, spec):
            if isinstance(obj, dict):
                if '$ref' in obj:
                    # Extract reference path
                    ref_path = obj['$ref'].split('/')
                    if ref_path[0] == '#':
                        resolved = spec
                        for part in ref_path[1:]:
                            resolved = resolved.get(part, {})
                        return resolve_refs(resolved, spec)
                else:
                    return {k: resolve_refs(v, spec) for k, v in obj.items()}
            elif isinstance(obj, list):
                return [resolve_refs(item, spec) for item in obj]
            else:
                return obj

        return resolve_refs(schema, spec)

    def validate_response(self, response_data: dict, schema_name: str) -> tuple[bool, list]:
        """Validate response data against schema."""
        if schema_name not in self.validators:
            return True, []  # No validator available, assume valid

        validator = self.validators[schema_name]
        errors = list(validator.iter_errors(response_data))
        return len(errors) == 0, errors


class RosettaClient:
    """Rosetta API client with built-in validation."""

    def __init__(self, base_url: str = "http://localhost:8082", validate_schemas: bool = True):
        self.base_url = base_url
        # Set timeout to 10 minutes (600 seconds) for slow endpoints
        self.client = httpx.Client(timeout=httpx.Timeout(600.0))
        self.validate_schemas = validate_schemas
        self.validator = SchemaValidator() if validate_schemas else None

    def search_transactions(self, **kwargs) -> httpx.Response:
        """
        Search transactions endpoint.

        Parameters can include:
        - network: Network identifier (default: preprod)
        - limit: Maximum number of results
        - offset: Number of results to skip
        - max_block: Maximum block height
        - status: Transaction status filter
        - success: Success boolean filter
        - type: Operation type filter
        - account_identifier: Account filter
        - transaction_identifier: Transaction filter
        - currency: Currency filter
        - operator: Logical operator (and/or)
        """
        # Build request body
        body = {}

        # Handle network_identifier
        if 'skip_network' not in kwargs:
            body["network_identifier"] = {
                "blockchain": "cardano",
                "network": kwargs.pop("network", "preprod")
            }
        else:
            kwargs.pop('skip_network')

        # Add other parameters
        for key, value in kwargs.items():
            if value is not None:
                body[key] = value

        # Make request
        response = self.client.post(
            f"{self.base_url}/search/transactions",
            json=body,
            headers={"Content-Type": "application/json"}
        )

        # Validate response schema if enabled
        if self.validate_schemas and response.status_code == 200:
            try:
                response_data = response.json()
                is_valid, errors = self.validator.validate_response(
                    response_data,
                    'SearchTransactionsResponse'
                )
                if not is_valid:
                    # Attach validation errors to response for debugging
                    response.validation_errors = errors
            except Exception:
                pass  # Don't fail on validation errors, just record them

        return response

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.client.close()