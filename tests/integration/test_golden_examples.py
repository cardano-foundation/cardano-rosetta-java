#!/usr/bin/env python3
# /// script
# requires-python = ">=3.10"
# dependencies = ["pyyaml", "jsonschema", "rich"]
# ///
"""Run Rosetta golden fixtures, compare responses and report the results."""

from __future__ import annotations

import argparse
import glob
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from collections import Counter, defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed
from contextlib import ExitStack, nullcontext, redirect_stdout
from functools import partial
from itertools import chain
from pathlib import Path
from typing import Any, Callable, Dict, Iterator, List, Optional, Sequence, Tuple

import yaml
from jsonschema import Draft4Validator
from rich import box
from rich.console import Console, Group
from rich.live import Live
from rich.panel import Panel
from rich.table import Table
from rich.text import Text

DEFAULT_ROSETTA_URL = "http://localhost:8082"
DEFAULT_BASE_DIR = (Path(__file__).parent / "golden_examples/rosetta_java").resolve()
DEFAULT_OPENAPI_PATH = (
    Path(__file__).parent.parent.parent
    / "api/src/main/resources/rosetta-specifications-1.4.15/api.yaml"
).resolve()
HTTP_TIMEOUT_SECONDS = 180

RED, GREEN, YELLOW = "\033[91m", "\033[92m", "\033[93m"
BLUE, MAGENTA, CYAN = "\033[94m", "\033[95m", "\033[96m"
RESET, BOLD, DIM = "\033[0m", "\033[1m", "\033[2m"

VOLATILE_FIELDS = {
    "metadata": ["metadata.ttl", "metadata.protocol_parameters.protocol"],
    "preprocess": ["options.transaction_size"],
}
DATA_ENDPOINT_VOLATILE_FIELDS = {
    "search": [],
    "transactions": ["total_count"],
    "block": [],
    "account": [],
    "network": [],
    "mempool": [],
}
VOLATILE_PATH_PATTERNS = [
    re.compile(r"\.currency\.metadata\."),
    re.compile(r"\.currency\.metadata$"),
]


# OpenAPI validation


class SchemaValidator:
    def __init__(self, spec_path: Path):
        self.spec = yaml.safe_load(spec_path.read_text(encoding="utf-8"))
        self._response_validators: Dict[
            str, Dict[Optional[int], Callable[[Any], List[str]]]
        ] = {}
        self._prepare_validators()

    def _response_schema(self, endpoint: str, status_code: int) -> Dict[str, Any]:
        responses = self.spec["paths"][endpoint]["post"]["responses"]
        response = (
            responses.get(str(status_code))
            or responses.get("default")
            or responses["500" if status_code != 200 else "200"]
        )
        return response["content"]["application/json"]["schema"]

    def _prepare_validators(self) -> None:
        for endpoint, path in self.spec["paths"].items():
            responses = path["post"]["responses"]
            codes = {int(code) for code in responses if str(code).isdecimal()} | {200}
            validators = {
                code: self._compile_response(endpoint, code) for code in codes
            }
            # Unknown errors use default/500; 200 retains its own schema selection.
            validators[None] = self._compile_response(endpoint, -1)
            self._response_validators[endpoint] = validators

    def _compile_response(
        self, endpoint: str, status_code: int
    ) -> Callable[[Any], List[str]]:
        schema = self._response_schema(endpoint, status_code)
        validator = Draft4Validator(self._deref(schema))
        return partial(self._validate_instance, validator)

    @staticmethod
    def _validate_instance(validator: Any, instance: Any) -> List[str]:
        try:
            return [
                f"{'.'.join(str(part) for part in error.path) or '<root>'}: {error.message}"
                for error in validator.iter_errors(instance)
            ]
        except Exception as error:
            return [f"Validator error: {error}"]

    def validate_response(
        self, endpoint: str, status_code: int, instance: Any
    ) -> List[str]:
        validators = self._response_validators[endpoint]
        validate = validators.get(status_code, validators[None])
        return validate(instance)

    def describe_response_schema(self, endpoint: str, status_code: int) -> str:
        schema = self._response_schema(endpoint, status_code)
        if "$ref" in schema:
            return str(schema["$ref"])
        title = schema.get("title")
        return f"inline ({title})" if title else "inline"

    def _resolve_ref(self, ref: str) -> Dict[str, Any]:
        node = self.spec
        for part in ref[2:].split("/"):
            node = node[part.replace("~1", "/").replace("~0", "~")]
        return node

    def _deref(self, node: Any, seen: Optional[set] = None) -> Any:
        seen = seen or set()
        if isinstance(node, dict):
            if (
                "$ref" in node
                and isinstance(node["$ref"], str)
                and node["$ref"].startswith("#/")
            ):
                ref = node["$ref"]
                if ref in seen:
                    raise ValueError(f"Circular schema reference: {ref}")
                target = self._resolve_ref(ref)
                # Only ancestors are visited; sibling references keep their rules.
                return self._deref(target, seen | {ref})
            return {
                key: self._deref(value, seen) if key != "$ref" else value
                for key, value in node.items()
            }
        if isinstance(node, list):
            return [self._deref(value, seen) for value in node]
        return node

    def get_resolved_schema(self, endpoint: str, status_code: int) -> Dict[str, Any]:
        return self._deref(self._response_schema(endpoint, status_code))

    def _stype(self, schema: Dict[str, Any]) -> str:
        if "type" in schema:
            return str(schema["type"])
        if "properties" in schema:
            return "object"
        if "items" in schema:
            return "array"
        for key in ("oneOf", "anyOf", "allOf"):
            if key in schema:
                return key
        return "unknown"

    def summarize_schema(self, schema: Dict[str, Any]) -> str:
        kind = self._stype(schema)
        if kind == "object":
            required = ", ".join(schema.get("required", [])) or "—"
            properties = schema.get("properties", {}) or {}
            lines = ["type: object", f"required: {required}"]
            if properties:
                lines.append("props:")
                for key, value in properties.items():
                    lines.append(
                        f"  - {key}: {self._stype(value) if isinstance(value, dict) else type(value).__name__}"
                    )
            return "\n".join(lines)
        if kind == "array":
            items = schema.get("items")
            return f"type: array\nitems: {self._stype(items) if isinstance(items, dict) else 'unknown'}"
        if kind in ("oneOf", "anyOf", "allOf"):
            return f"{kind}({len(schema.get(kind, []))} variants)"
        return f"type: {kind}"


# HTTP transport


def replace_network_placeholder(
    payload: Dict[str, Any], network_id: Optional[str]
) -> Dict[str, Any]:
    text = json.dumps(payload)
    if "{{networkId}}" in text:
        network = network_id if network_id is not None else "preprod"
        return json.loads(text.replace("{{networkId}}", network))
    return payload


def call_api(
    endpoint: str, payload: Dict[str, Any], base_url: str, network_id: Optional[str]
) -> Tuple[Optional[Dict[str, Any]], int, Optional[str], Dict[str, Any]]:
    try:
        modified_payload = replace_network_placeholder(payload, network_id)
        request = urllib.request.Request(
            f"{base_url}{endpoint}",
            data=json.dumps(modified_payload).encode("utf-8"),
            headers={"Content-Type": "application/json"},
        )
        start = time.monotonic()
        with urllib.request.urlopen(request, timeout=HTTP_TIMEOUT_SECONDS) as response:
            raw = response.read()
            return (
                json.loads(raw.decode("utf-8")),
                response.code,
                None,
                {
                    "time_ms": (time.monotonic() - start) * 1000.0,
                    "size_bytes": len(raw),
                },
            )
    except urllib.error.HTTPError as error:
        try:
            raw = error.read()
            return (
                json.loads(raw.decode("utf-8")),
                error.code,
                None,
                {"time_ms": 0.0, "size_bytes": len(raw)},
            )
        except Exception:
            return (
                None,
                error.code,
                f"HTTP {error.code}: {error.reason}",
                {"time_ms": 0.0, "size_bytes": 0},
            )
    except urllib.error.URLError as error:
        return (
            None,
            0,
            f"Connection error: {error.reason}",
            {"time_ms": 0.0, "size_bytes": 0},
        )
    except Exception as error:
        return None, 0, f"Unexpected error: {error}", {"time_ms": 0.0, "size_bytes": 0}


# Response comparisons. These functions perform no I/O.


def get_ignore_paths(endpoint: str) -> List[str]:
    key = endpoint.split("/")[-1]
    return VOLATILE_FIELDS.get(key, DATA_ENDPOINT_VOLATILE_FIELDS.get(key, []))


def deep_diff(
    expected: Any,
    actual: Any,
    path: str = "",
    ignore_paths: Optional[List[str]] = None,
    *,
    ignore_token_decimals: bool = False,
) -> List[str]:
    if any(path.startswith(prefix) for prefix in ignore_paths or []):
        return []
    if any(pattern.search(path) for pattern in VOLATILE_PATH_PATTERNS):
        return []
    if expected is None and actual is None:
        return []
    if expected is None or actual is None:
        return [f"{path}: Expected {json.dumps(expected)} but got {json.dumps(actual)}"]
    if type(expected) is not type(actual):
        return [
            f"{path}: Type mismatch - expected {type(expected).__name__} but got {type(actual).__name__}"
        ]
    if isinstance(expected, dict):
        differences: List[str] = []
        expected_keys, actual_keys = set(expected), set(actual)
        for key in expected_keys - actual_keys:
            differences.append(f"{path}.{key}: Missing in actual response")
        for key in actual_keys - expected_keys:
            differences.append(f"{path}.{key}: Unexpected in actual response")
        for key in expected_keys & actual_keys:
            if (
                ignore_token_decimals
                and key == "decimals"
                and path.split(".")[-1] == "currency"
                and expected.get("symbol") != "ADA"
            ):
                continue
            child = f"{path}.{key}" if path else key
            differences.extend(
                deep_diff(
                    expected[key],
                    actual[key],
                    child,
                    ignore_paths,
                    ignore_token_decimals=ignore_token_decimals,
                )
            )
        return differences
    if isinstance(expected, list):
        if len(expected) != len(actual):
            return [
                f"{path}: List length mismatch - expected {len(expected)} but got {len(actual)}"
            ]
        return [
            difference
            for index, (expected_item, actual_item) in enumerate(zip(expected, actual))
            for difference in deep_diff(
                expected_item,
                actual_item,
                f"{path}[{index}]",
                ignore_paths,
                ignore_token_decimals=ignore_token_decimals,
            )
        ]
    return (
        []
        if expected == actual
        else [f"{path}: Expected {json.dumps(expected)} but got {json.dumps(actual)}"]
    )


def _normalize_req_ids(message: str) -> str:
    return re.sub(r"request \d+:", "request [ID]:", message)


def _success_differences(
    expected: Any,
    endpoint: str,
    actual: Any,
    http_status: int,
    schema_errors: Sequence[str],
) -> List[str]:
    if http_status != 200:
        error = (
            (actual.get("error", actual) if isinstance(actual, dict) else {})
            if actual
            else {}
        )
        code, message = (
            error.get("code", "unknown"),
            error.get("message", "unknown message"),
        )
        details = error.get("details", {})
        detail_message = (
            f" - {details.get('message')}"
            if isinstance(details, dict) and "message" in details
            else (f" - {details}" if isinstance(details, str) else "")
        )
        return [
            f"Expected successful response but got ERROR {code}: {message}{detail_message} (HTTP {http_status})"
        ]
    differences = deep_diff(
        expected,
        actual,
        "",
        get_ignore_paths(endpoint),
        ignore_token_decimals=not endpoint.startswith("/construction/"),
    )
    differences.extend(f"Schema: {error}" for error in schema_errors)
    return differences


def _error_differences(
    expected: Dict[str, Any],
    actual: Any,
    http_status: int,
    schema_errors: Sequence[str],
) -> List[str]:
    if http_status == 200:
        return ["Expected error but got successful response"]
    error = actual.get("error", actual) if isinstance(actual, dict) else actual
    if not isinstance(error, dict):
        return ["Expected an error object"] + [
            f"Schema: {message}" for message in schema_errors
        ]
    differences = [
        difference
        for field in ("code", "message", "retriable")
        if field in expected
        for difference in deep_diff(expected[field], error.get(field), field)
    ]
    if "retriable" in expected:
        # GlobalExceptionHandler maps the expected retry flag to HTTP 400/500.
        expected_status = 500 if expected["retriable"] else 400
        if http_status != expected_status:
            differences.append(
                f"HTTP status: expected {expected_status} but got {http_status}"
            )
    if (
        "details" in expected
        and "details" in error
        and isinstance(expected["details"], dict)
        and isinstance(error["details"], dict)
    ):
        expected_message, actual_message = (
            expected["details"].get("message"),
            error["details"].get("message"),
        )
        if (
            isinstance(expected_message, str)
            and isinstance(actual_message, str)
            and _normalize_req_ids(expected_message)
            != _normalize_req_ids(actual_message)
        ):
            differences.append(
                f'details.message: Expected "{expected_message}" but got "{actual_message}"'
            )
    differences.extend(f"Schema: {error}" for error in schema_errors)
    return differences


def evaluate_response(
    fixture: Dict[str, Any],
    endpoint: str,
    actual: Any,
    http_status: int,
    schema_errors: Sequence[str] = (),
) -> Tuple[str, List[str]]:
    if "expected_response" in fixture:
        differences = _success_differences(
            fixture["expected_response"], endpoint, actual, http_status, schema_errors
        )
    else:
        differences = _error_differences(
            fixture["expected_error"], actual, http_status, schema_errors
        )
    return ("failed" if differences else "passed"), differences


# Fixture loading and execution


def collect_files(paths: List[str], base_dir: Path) -> List[Path]:
    """Select JSON files from file paths, recursive directories or glob filters."""
    files = set()
    for pattern in paths or [str(base_dir)]:
        local_exists = Path(pattern).exists()
        # Existing local paths keep priority over base_dir.
        locations = (
            (pattern,) if local_exists else (pattern, os.path.join(base_dir, pattern))
        )
        for location in locations:
            path = Path(location)
            matches = glob.iglob(
                glob.escape(location) if local_exists else location, recursive=True
            )
            directory_files = path.rglob("*.json") if path.is_dir() else ()
            candidates = chain(map(Path, matches), directory_files)
            files.update(
                candidate.resolve()
                for candidate in candidates
                if candidate.suffix == ".json" and candidate.is_file()
            )
    return sorted(files)


def determine_endpoint(path: Path, base_dir: Path, fixture: Any) -> str:
    """Prefer the declared endpoint; otherwise use the fixture-root layout."""
    if isinstance(fixture, dict) and "endpoint" in fixture:
        return fixture["endpoint"]
    parts = path.resolve().relative_to(base_dir.resolve()).parts
    endpoint = parts[1] if parts[0] == "construction" else parts[0]
    return f"/construction/{endpoint}"


def load_fixture(path: Path, base_dir: Path) -> Dict[str, Any]:
    fixture, error = None, None
    try:
        fixture = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as failure:
        error = f"Invalid JSON in file: {failure}"
    except Exception as failure:
        error = f"Unexpected error: {failure}"
    return {
        "path": path,
        "endpoint": determine_endpoint(path, base_dir, fixture),
        "fixture": fixture,
        "read_error": error,
    }


def _fixture_skip_reason(
    fixture: Dict[str, Any], network_id: Optional[str]
) -> Tuple[bool, Any]:
    if "skip_reason" in fixture:
        return True, fixture["skip_reason"]
    if "network" in fixture and network_id and fixture["network"] != network_id:
        return (
            True,
            f"Requires network '{fixture['network']}', running on '{network_id}'",
        )
    if "request_body" not in fixture:
        return True, "No request_body found"
    if not ("expected_response" in fixture or "expected_error" in fixture):
        return True, fixture.get(
            "description", "No expected_response or expected_error found"
        )
    return False, None


def _schema_info(
    endpoint: str,
    http_status: int,
    actual: Any,
    validator: SchemaValidator,
    show_details: bool,
) -> Dict[str, Any]:
    errors = validator.validate_response(endpoint, http_status, actual)
    summary = (
        validator.summarize_schema(validator.get_resolved_schema(endpoint, http_status))
        if show_details
        else None
    )
    return {
        "status": "failed" if errors else "passed",
        "endpoint": endpoint,
        "http_status": http_status,
        "descriptor": validator.describe_response_schema(endpoint, http_status),
        "errors": errors,
        "summary": summary,
    }


def validate_fixture(
    case: Dict[str, Any],
    options: argparse.Namespace,
    validator: SchemaValidator,
) -> Dict[str, Any]:
    result: Dict[str, Any] = {
        "file": str(case["path"].relative_to(options.base_dir)),
        "status": "unknown",
        "differences": [],
        "error": None,
    }
    if case["read_error"]:
        return dict(result, status="error", error=case["read_error"])
    try:
        fixture, endpoint = case["fixture"], case["endpoint"]
        skipped, reason = _fixture_skip_reason(fixture, options.network_id)
        if skipped:
            return dict(result, status="skipped", error=reason)
        actual, http_status, error, metrics = call_api(
            endpoint, fixture["request_body"], options.url, options.network_id
        )
        if error:
            return dict(result, status="error", error=f"API call failed: {error}")
        schema_info = _schema_info(
            endpoint, http_status, actual, validator, options.schema_details
        )
        result["schema_info"] = schema_info
        result["metrics"] = {
            "status": http_status,
            "time_ms": metrics["time_ms"],
            "size_bytes": metrics["size_bytes"],
        }
        if "expected_error" in fixture:
            fixture = dict(
                fixture,
                expected_error=replace_network_placeholder(
                    fixture["expected_error"], options.network_id
                ),
            )
        result["status"], result["differences"] = evaluate_response(
            fixture, endpoint, actual, http_status, schema_info["errors"]
        )
    except Exception as failure:
        result.update(status="error", error=f"Unexpected error: {failure}")
    return result


def group_fixtures(
    fixtures: List[Dict[str, Any]], base_dir: Path
) -> Iterator[Tuple[str, Dict[str, List[Dict[str, Any]]]]]:
    endpoints: Dict[str, List[Dict[str, Any]]] = defaultdict(list)
    for case in fixtures:
        endpoints[case["endpoint"]].append(case)
    for endpoint in sorted(endpoints):
        groups: Dict[str, List[Dict[str, Any]]] = defaultdict(list)
        for case in sorted(endpoints[endpoint], key=lambda case: case["path"]):
            relative = case["path"].resolve().relative_to(base_dir.resolve())
            name = relative.parts[1] if len(relative.parts) > 1 else "_root"
            groups[name].append(case)
        yield endpoint, dict(sorted(groups.items()))


def run_group(
    cases: List[Dict[str, Any]],
    options: argparse.Namespace,
    validator: SchemaValidator,
    ordered: bool,
) -> Iterator[Dict[str, Any]]:
    with ThreadPoolExecutor(max_workers=options.workers) as executor:
        futures = {
            executor.submit(validate_fixture, case, options, validator): index
            for index, case in enumerate(cases)
        }
        completed = (
            (futures[future], future.result()) for future in as_completed(futures)
        )
        if ordered:
            completed = iter(sorted(completed, key=lambda item: item[0]))
        for _, result in completed:
            yield result


# Presentation. Both output modes consume the same results.


def _fmt_bytes(value: int) -> str:
    size = float(value)
    for unit in ("B", "KB", "MB"):
        if size < 1024.0:
            return f"{int(size)} {unit}" if unit == "B" else f"{size:.1f} {unit}"
        size /= 1024.0
    return f"{size:.1f} GB"


def _fmt_ms(value: float) -> str:
    return f"{value:.0f} ms" if value < 1000 else f"{value / 1000.0:.2f} s"


def _schema_desc(descriptor: str) -> str:
    return (
        descriptor.split("/")[-1]
        if isinstance(descriptor, str)
        and descriptor.startswith("#/components/schemas/")
        else descriptor
    )


def _status_text(status: str) -> str:
    return {
        "passed": f"{GREEN}PASSED{RESET}",
        "failed": f"{RED}FAILED{RESET}",
        "skipped": f"{YELLOW}SKIPPED{RESET}",
    }.get(status, f"{MAGENTA}ERROR{RESET}")


def _rich_group(
    endpoint: str, name: str, validator: SchemaValidator
) -> Tuple[Any, Any]:
    success = validator.summarize_schema(validator.get_resolved_schema(endpoint, 200))
    error = validator.summarize_schema(validator.get_resolved_schema(endpoint, 500))
    schemas = Table(box=box.SIMPLE, expand=True)
    schemas.add_column("Success schema", style="green", overflow="fold")
    schemas.add_column("Error schema", style="yellow", overflow="fold")
    schemas.add_row(success, error)
    schemas_panel = Panel(schemas, title="Schemas", border_style="cyan")
    table = Table.grid(expand=True, padding=(1, 1))
    for label, ratio in [
        ("File", 6),
        ("Status", 2),
        ("HTTP", 1),
        ("Time", 1),
        ("Size", 1),
        ("Schema", 4),
    ]:
        table.add_column(label, ratio=ratio, overflow="fold")
    table.add_row(
        *(
            Text(label, style="bold")
            for label in ["File", "Status", "HTTP", "Time", "Size", "Schema"]
        )
    )
    root = Panel(
        Group(schemas_panel, table),
        title=f"{endpoint.split('/')[-1]}/{name}",
        border_style="white",
        expand=True,
    )
    return table, root


def _print_rich_result(result: Dict[str, Any], table: Any, console: Any) -> None:
    schema, metrics = result.get("schema_info", {}), result.get("metrics", {})
    filename = Path(result["file"]).name
    table.add_row(
        filename,
        _status_text(result["status"]),
        str(metrics.get("status", "")),
        _fmt_ms(metrics.get("time_ms", 0)),
        _fmt_bytes(metrics.get("size_bytes", 0)),
        _schema_desc(schema.get("descriptor", "")),
    )
    if result["status"] == "failed" and result.get("differences"):
        details = Text()
        for difference in result["differences"]:
            details.append(f"• {difference}\n", style="red")
        console.print(Panel(details, title=f"Details: {filename}", border_style="red"))
    elif result["status"] == "error" and result.get("error"):
        console.print(
            Panel(
                Text(str(result["error"])),
                title=f"Error: {filename}",
                border_style="magenta",
            )
        )


def print_result(
    result: Dict[str, Any],
    options: argparse.Namespace,
    table: Any = None,
    console: Any = None,
) -> None:
    if table is not None:
        _print_rich_result(result, table, console)
        return
    status = result["status"]
    color, symbol = {
        "passed": (GREEN, "."),
        "failed": (RED, "F"),
        "skipped": (YELLOW, "S"),
    }.get(status, (MAGENTA, "E"))
    if not options.verbose:
        sys.stdout.write(f"{color}{symbol}{RESET}")
        sys.stdout.flush()
        return
    print(f"  {result['file']}: {color}{status.upper()}{RESET}")
    schema = result.get("schema_info", {})
    if schema:
        print(
            f"    - Schema: {_schema_desc(schema['descriptor'])} [{schema['status'].upper()}]"
        )
        if options.schema_details and schema.get("summary"):
            print(f"    - Schema details: {schema['summary']}")
    metrics = result.get("metrics", {})
    if metrics:
        print(
            f"    - HTTP: {metrics.get('status', '')} • Time: {_fmt_ms(metrics.get('time_ms', 0))} • Size: {_fmt_bytes(metrics.get('size_bytes', 0))}"
        )
    if status == "failed" and result.get("differences"):
        for difference in result["differences"]:
            print(f"    - {difference}")
    elif status == "error" and result.get("error"):
        print(f"    - {result['error']}")


def result_counts(results: List[Dict[str, Any]]) -> Counter:
    counts = Counter(result["status"] for result in results)
    counts["total"] = len(results)
    counts["errors"] = (
        len(results) - counts["passed"] - counts["failed"] - counts["skipped"]
    )
    return counts


def print_endpoint_summary(
    endpoint: str, results: List[Dict[str, Any]], console: Any
) -> Dict[str, Any]:
    counts = result_counts(results)
    total = counts["total"]
    rate = counts["passed"] / total * 100.0 if total else 0.0
    timings = [
        float(result.get("metrics", {}).get("time_ms", 0.0) or 0.0)
        for result in results
    ]
    elapsed = sum(timings)
    size = sum(
        int(result.get("metrics", {}).get("size_bytes", 0) or 0) for result in results
    )
    slowest = max(zip(timings, results), key=lambda item: item[0], default=(0.0, None))
    color = GREEN if rate >= 90 else (YELLOW if rate >= 50 else RED)
    first_line = (
        f"Files {CYAN}{total}{RESET} • Passed {GREEN}{counts['passed']}{RESET} • "
        f"Failed {RED}{counts['failed']}{RESET} • Skipped {YELLOW}{counts['skipped']}{RESET} • "
        f"Errors {MAGENTA}{counts['errors']}{RESET} • Pass {color}{rate:.1f}%{RESET}"
    )
    second_line = (
        f"Total {CYAN}{_fmt_ms(elapsed)}{RESET} • Avg {CYAN}{_fmt_ms(elapsed / total if total else 0.0)}{RESET} • "
        f"Data {CYAN}{_fmt_bytes(size)}{RESET}"
    )
    if slowest[0] > 0:
        second_line += f" • Slowest {BLUE}{Path(slowest[1]['file']).name}{RESET} ({YELLOW}{_fmt_ms(slowest[0])}{RESET})"
    if console is not None:
        console.print(
            Panel(
                Text.from_ansi(first_line + "\n" + second_line),
                title=f"\nSummary {endpoint}",
                border_style="white",
                expand=True,
            )
        )
    else:
        print(f"\n  Summary: {first_line}\n  Time: {second_line}")
    for status, title in [
        ("failed", "Failing tests"),
        ("error", "Errors"),
        ("skipped", "Skipped tests"),
    ]:
        cases = [result for result in results if result["status"] == status]
        if cases:
            print(f"\n  {BOLD}{title}:{RESET}")
        for result in cases:
            if status == "failed" and result.get("differences"):
                print(
                    f"    - {BLUE}{result['file']}{RESET} ::\n      {result['differences'][0]}"
                )
            elif status != "failed":
                reason = result.get("error") or (
                    "skipped" if status == "skipped" else "error"
                )
                print(
                    f"    - {BLUE}{result['file']}{RESET} :: {YELLOW if status == 'skipped' else MAGENTA}{reason}{RESET}"
                )
    return {
        "endpoint": endpoint,
        **{key: counts[key] for key in ["passed", "failed", "skipped", "errors"]},
    }


def print_summary(statistics: Dict[str, Any], elapsed_time: float = 0) -> None:
    counts = result_counts(statistics["results"])
    schemas = Counter(
        result["schema_info"]["status"]
        for result in statistics["results"]
        if "schema_info" in result
    )
    total, passed, failed = counts["total"], counts["passed"], counts["failed"]
    skipped, errors = counts["skipped"], counts["errors"]
    rate = passed / total * 100 if total else 0
    print("\n" + "=" * 80)
    print(f"{BOLD}VALIDATION SUMMARY{RESET}")
    print("=" * 80)
    print(f"\nTotal files found: {statistics['total_files']}\nFiles tested: {total}")
    print(f"  {GREEN}✓ Passed: {passed} ({rate:.1f}%){RESET}")
    print(
        f"  {RED}✗ Failed: {failed}{RESET}\n  {YELLOW}○ Skipped: {skipped}{RESET}\n  {MAGENTA}⚠ Errors: {errors}{RESET}"
    )
    print(f"\n{BOLD}Schema validation:{RESET}")
    print(
        f"  checked: {schemas['passed'] + schemas['failed']}, {GREEN}passed: {schemas['passed']}{RESET}, "
        f"{RED}failed: {schemas['failed']}{RESET}"
    )
    print(f"\n{BOLD}Per-endpoint status:{RESET}")
    for summary in statistics["endpoint_summaries"]:
        endpoint_total = sum(
            summary[key] for key in ["passed", "failed", "skipped", "errors"]
        )
        print(
            f"  {summary['endpoint']}: {GREEN}{summary['passed']}{RESET}/{endpoint_total} passed, "
            f"{RED}{summary['failed']}{RESET} failed, {YELLOW}{summary['skipped']}{RESET} skipped, "
            f"{MAGENTA}{summary['errors']}{RESET} errors"
        )
    if elapsed_time > 0:
        print(f"\n{BOLD}Total execution time:{RESET} {CYAN}{elapsed_time:.2f}s{RESET}")
    print("\n" + "=" * 80)
    print(
        f"{GREEN if failed == 0 and errors == 0 else RED}{BOLD}{'✓ RESULT: PASS' if failed == 0 and errors == 0 else '✗ RESULT: FAIL'}{RESET}"
    )


# CLI and orchestration


def parse_args(argv: Optional[List[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Validate Rosetta golden test files against a running API",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=(
            "Examples:\n"
            "  uv run test_golden_examples.py\n"
            "  uv run test_golden_examples.py -u http://localhost:8080 -v\n"
            "  uv run test_golden_examples.py 'construction/preprocess/**/*.json'\n"
            "  uv run test_golden_examples.py -j 20\n"
            "  uv run test_golden_examples.py --network-id devkit\n"
        ),
    )
    parser.add_argument(
        "paths",
        nargs="*",
        help="File paths or glob patterns (relative to CWD or --base-dir)",
    )
    parser.add_argument(
        "--base-dir",
        type=Path,
        default=DEFAULT_BASE_DIR,
        help="Root directory with construction tests",
    )
    parser.add_argument(
        "-u", "--url", default=DEFAULT_ROSETTA_URL, help="Rosetta API base URL"
    )
    parser.add_argument(
        "--openapi",
        type=Path,
        default=DEFAULT_OPENAPI_PATH,
        help="OpenAPI spec file (YAML/JSON)",
    )
    parser.add_argument("-o", "--output", help="Write full report to this file")
    parser.add_argument(
        "-v", "--verbose", action="store_true", help="Show per-file details"
    )
    parser.add_argument(
        "--schema-details",
        action="store_true",
        help="Show concise schema details per test",
    )
    parser.add_argument(
        "-j",
        "--workers",
        type=int,
        default=10,
        help="Number of parallel workers for test execution (default: 10)",
    )
    parser.add_argument(
        "-n",
        "--network-id",
        default="preprod",
        help="Network ID to replace {{networkId}} placeholders in test files (e.g., 'devkit', 'preprod', 'mainnet')",
    )
    options = parser.parse_args(argv)
    options.base_dir = options.base_dir.resolve()
    options.schema_details = bool(options.schema_details or options.verbose)
    return options


def execute(
    options: argparse.Namespace,
    validator: SchemaValidator,
    console: Any,
    statistics: Dict[str, Any],
) -> None:
    print("\nScanning for test files...")
    files = collect_files(options.paths, options.base_dir)
    statistics["total_files"] = len(files)
    print(f"Found {len(files)} test files\n")
    fixtures = [load_fixture(path, options.base_dir) for path in files]
    for endpoint, groups in group_fixtures(fixtures, options.base_dir):
        print(
            f"\n{BOLD}Testing {endpoint} ({sum(len(cases) for cases in groups.values())} files){RESET}"
        )
        print("-" * 40)
        endpoint_results: List[Dict[str, Any]] = []
        for name, cases in groups.items():
            table, live = None, nullcontext()
            if console is not None:
                table, root = _rich_group(endpoint, name, validator)
                live = Live(root, console=console, refresh_per_second=8)
            else:
                print(f"\n  {DIM}[{endpoint.split('/')[-1]}/{name}]{RESET}")
            with live:
                for result in run_group(
                    cases, options, validator, ordered=console is not None
                ):
                    statistics["results"].append(result)
                    endpoint_results.append(result)
                    print_result(result, options, table, console)
        statistics["endpoint_summaries"].append(
            print_endpoint_summary(endpoint, endpoint_results, console)
        )


def main(argv: Optional[List[str]] = None) -> int:
    options = parse_args(argv)
    start = time.time()
    statistics: Dict[str, Any] = {
        "total_files": 0,
        "results": [],
        "endpoint_summaries": [],
    }
    try:
        with ExitStack() as stack:
            if options.output:
                output = stack.enter_context(
                    open(options.output, "w", encoding="utf-8")
                )
                stack.enter_context(redirect_stdout(output))
            console = Console() if options.verbose else None
            validator = SchemaValidator(options.openapi)
            execute(options, validator, console, statistics)
            print_summary(statistics, time.time() - start)
    except KeyboardInterrupt:
        print(f"\n{YELLOW}Validation interrupted by user{RESET}")
        print_summary(statistics, 0)
        return 1
    except Exception as failure:
        print(f"\n{RED}Fatal error: {failure}{RESET}")
        return 1
    counts = result_counts(statistics["results"])
    return int(counts["failed"] > 0 or counts["errors"] > 0)


if __name__ == "__main__":
    sys.exit(main())
