#!/usr/bin/env python3
# /// script
# requires-python = ">=3.8"
# dependencies = [
#   "pyyaml",
#   "jsonschema",
#   "rich"
# ]
# ///
"""
Lean monolithic validator for Rosetta construction tests.
Preserves features: snapshot diffs, HTTP status checks, optional OpenAPI
schema validation (pyyaml/jsonschema), optional rich output, grouping,
metrics, CLI options, and detailed final summary.
"""

from __future__ import annotations

import argparse
import copy
import glob
import json
import os
import sys
import time
import urllib.error
import urllib.request
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

# Optional deps (graceful degradation)
try:
    import yaml  # type: ignore
except Exception:  # pragma: no cover
    yaml = None  # type: ignore

try:
    import jsonschema  # type: ignore
    from jsonschema import Draft4Validator  # type: ignore
except Exception:  # pragma: no cover
    jsonschema = None  # type: ignore
    Draft4Validator = None  # type: ignore

try:
    from rich.console import Console  # type: ignore
    from rich.console import Group  # type: ignore
    from rich.table import Table  # type: ignore
    from rich.text import Text  # type: ignore
    from rich.live import Live  # type: ignore
    from rich.panel import Panel  # type: ignore
    from rich import box  # type: ignore
except Exception:  # pragma: no cover
    Console = Group = Table = Text = Live = Panel = box = None  # type: ignore

# Defaults
DEFAULT_ROSETTA_URL = "http://localhost:8082"
DEFAULT_BASE_DIR = (
    Path(__file__).parent / "golden_examples/rosetta_java"
).resolve()
DEFAULT_OPENAPI_PATH = (
    Path(__file__).parent.parent.parent
    / "api/src/main/resources/rosetta-specifications-1.4.15/api.yaml"
).resolve()

# Globals updated via CLI
BASE_PATH = DEFAULT_BASE_DIR
ROSETTA_URL = DEFAULT_ROSETTA_URL
VERBOSE = False
SCHEMA_VALIDATION_ENABLED = True
OPENAPI_SPEC_PATH: Optional[Path] = DEFAULT_OPENAPI_PATH
SCHEMA_VALIDATOR: Optional["SchemaValidator"] = None
SHOW_SCHEMA_DETAILS = False
RICH_CONSOLE: Optional["Console"] = None  # type: ignore
USE_RICH = False
WORKERS = 10
NETWORK_ID: Optional[str] = None

# ANSI colors
RED = "\033[91m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
MAGENTA = "\033[95m"
CYAN = "\033[96m"
WHITE = "\033[97m"
RESET = "\033[0m"
BOLD = "\033[1m"
DIM = "\033[2m"
GRAY = "\033[90m"

# Fields that should be ignored when diffing
# Construction endpoints
VOLATILE_FIELDS = {
    "metadata": ["metadata.ttl", "metadata.protocol_parameters.protocol"],
    "preprocess": ["options.transaction_size"],
}

# Data endpoints - add volatile fields if needed
# (e.g., timestamps that change between runs)
DATA_ENDPOINT_VOLATILE_FIELDS = {
    "search": [],
    "transactions": ["total_count"],
    "block": [],
    "account": [],
    "network": [],
    "mempool": [],
}

# Regex patterns for paths that should be ignored during diffing.
# Token registry enrichment adds decimals/metadata fields that vary by environment.
import re
VOLATILE_PATH_PATTERNS = [
    re.compile(r"\.currency\.decimals$"),
    re.compile(r"\.currency\.metadata\."),
    re.compile(r"\.currency\.metadata$"),
]

# Stats
stats: Dict[str, Any] = {
    "total_files": 0,
    "files_tested": 0,
    "files_passed": 0,
    "files_failed": 0,
    "files_skipped": 0,
    "errors_by_endpoint": defaultdict(int),
    "differences_found": [],
    "endpoint_summaries": [],
    "schema_checked": 0,
    "schema_passed": 0,
    "schema_failed": 0,
    "schema_skipped": 0,
    "schema_unavailable": 0,
}


# --------------------- Schema Validation ---------------------
class SchemaValidator:
    def __init__(self, spec_path: Path, enabled: bool = True):
        self.enabled = enabled
        self.spec_path = spec_path
        self.spec: Optional[Dict[str, Any]] = None
        if enabled:
            try:
                txt = (
                    spec_path.read_text(encoding="utf-8")
                    if spec_path.exists()
                    else None
                )
                if not txt:
                    self.enabled = False
                    return
                self.spec = yaml.safe_load(txt) if yaml is not None else json.loads(txt)
            except Exception:
                try:
                    self.spec = json.loads(txt or "{}")
                except Exception:
                    self.enabled = False
                    self.spec = None

    def _path_item(self, endpoint: str) -> Optional[Dict[str, Any]]:
        if not (self.enabled and isinstance(self.spec, dict)):
            return None
        return (self.spec.get("paths") or {}).get(endpoint)

    def _response_schema(
        self, endpoint: str, status_code: int
    ) -> Optional[Dict[str, Any]]:
        p = self._path_item(endpoint)
        if not p:
            return None
        responses = (p.get("post") or {}).get("responses") or {}
        obj = (
            responses.get(str(status_code))
            or responses.get("default")
            or (responses.get("500") if status_code != 200 else None)
        )
        content = (obj or {}).get("content") or {}
        app = content.get("application/json") or {}
        if isinstance(app, dict) and "schema" in app:
            return app["schema"]
        for v in content.values():
            if isinstance(v, dict) and "schema" in v:
                return v["schema"]
        return None

    def describe_response_schema(self, endpoint: str, status_code: int) -> str:
        if not self.enabled or self.spec is None:
            return "unavailable"
        s = self._response_schema(endpoint, status_code)
        if not s:
            return "none"
        if isinstance(s, dict) and "$ref" in s:
            return str(s["$ref"])  # e.g. #/components/schemas/Thing
        t = s.get("title") if isinstance(s, dict) else None
        return f"inline ({t})" if t else "inline"

    def _resolve_ref(self, ref: str) -> Optional[Dict[str, Any]]:
        if not (isinstance(ref, str) and ref.startswith("#/")) or self.spec is None:
            return None
        node: Any = self.spec
        try:
            for part in ref.lstrip("#/").split("/"):
                node = node.get(part) if isinstance(node, dict) else None
                if node is None:
                    return None
            return node if isinstance(node, dict) else None
        except Exception:
            return None

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
                    return {}
                seen.add(ref)
                target = self._resolve_ref(ref)
                return self._deref(
                    copy.deepcopy(target) if isinstance(target, dict) else node, seen
                )
            return {
                k: self._deref(v, seen) if k != "$ref" else v for k, v in node.items()
            }
        if isinstance(node, list):
            return [self._deref(v, seen) for v in node]
        return node

    def _basic_sanity(self, instance: Any) -> List[str]:
        if instance is None:
            return ["<root>: Response is null"]
        if not isinstance(instance, (dict, list)):
            return [
                f"<root>: Response type must be object/array, got {type(instance).__name__}"
            ]
        return []

    def get_resolved_schema(
        self, endpoint: str, status_code: int
    ) -> Optional[Dict[str, Any]]:
        if not (self.enabled and self.spec):
            return None
        base = self._response_schema(endpoint, status_code)
        return self._deref(base) if isinstance(base, dict) else None

    def _stype(self, s: Dict[str, Any]) -> str:
        if "type" in s:
            return str(s["type"])
        if "properties" in s:
            return "object"
        if "items" in s:
            return "array"
        for k in ("oneOf", "anyOf", "allOf"):
            if k in s:
                return k
        return "unknown"

    def summarize_schema(self, s: Optional[Dict[str, Any]]) -> Optional[str]:
        if not isinstance(s, dict):
            return None
        t = self._stype(s)
        if t == "object":
            req = ", ".join(s.get("required", [])) or "—"
            props = s.get("properties", {}) or {}
            lines = ["type: object", f"required: {req}"]
            if props:
                lines.append("props:")
                for k, v in props.items():
                    lines.append(
                        f"  - {k}: {self._stype(v) if isinstance(v, dict) else type(v).__name__}"
                    )
            return "\n".join(lines)
        if t == "array":
            it = s.get("items")
            return f"type: array\nitems: {self._stype(it) if isinstance(it, dict) else 'unknown'}"
        if t in ("oneOf", "anyOf", "allOf"):
            return f"{t}({len(s.get(t, []))} variants)"
        return f"type: {t}"

    def validate_response(
        self, endpoint: str, status_code: int, instance: Any
    ) -> List[str]:
        if not self.enabled:
            return []
        if self.spec is None:
            return self._basic_sanity(instance)
        schema = self._response_schema(endpoint, status_code)
        if not schema:
            return self._basic_sanity(instance)
        if jsonschema is None:
            return self._basic_sanity(instance)
        try:
            d = self._deref(schema)
            if Draft4Validator:
                errs: List[str] = []
                for e in Draft4Validator(d).iter_errors(instance):
                    loc = ".".join([str(p) for p in e.path]) or "<root>"
                    errs.append(f"{loc}: {e.message}")
                return errs
            jsonschema.validate(instance=instance, schema=d)  # type: ignore
            return []
        except Exception as e:
            return [f"Validator error: {e}"]


# --------------------- HTTP + Diff utils ---------------------
def replace_network_placeholder(payload: Dict[str, Any]) -> Tuple[Dict[str, Any], bool]:
    """Replace {{networkId}} placeholders with actual network_id value.
    Uses 'preprod' as default if no network_id is provided.

    Returns:
        Tuple of (modified_payload, has_placeholder)
    """
    try:
        # Convert to JSON string for safe replacement
        json_str = json.dumps(payload)

        # Check if placeholder exists
        has_placeholder = "{{networkId}}" in json_str

        if has_placeholder:
            # Use provided network_id or default to 'preprod'
            network_value = NETWORK_ID if NETWORK_ID is not None else "preprod"
            json_str = json_str.replace("{{networkId}}", network_value)
            # Parse back to dict
            return json.loads(json_str), True

        return payload, False
    except Exception:
        # If anything goes wrong, return original payload
        return payload, False


def call_api(
    endpoint: str, payload: Dict[str, Any]
) -> Tuple[Optional[Dict[str, Any]], int, Optional[str], Dict[str, Any]]:
    url = f"{ROSETTA_URL}{endpoint}"
    try:
        # Replace network placeholders if needed
        modified_payload, _ = replace_network_placeholder(payload)
        data = json.dumps(modified_payload).encode("utf-8")
        req = urllib.request.Request(
            url, data=data, headers={"Content-Type": "application/json"}
        )
        start = time.monotonic()
        with urllib.request.urlopen(req, timeout=90) as resp:
            raw = resp.read()
            return (
                json.loads(raw.decode("utf-8")),
                resp.code,
                None,
                {
                    "time_ms": (time.monotonic() - start) * 1000.0,
                    "size_bytes": len(raw),
                },
            )
    except urllib.error.HTTPError as e:
        try:
            raw = e.read()
            body = json.loads(raw.decode("utf-8"))
            return body, e.code, None, {"time_ms": 0.0, "size_bytes": len(raw)}
        except Exception:
            return (
                None,
                e.code,
                f"HTTP {e.code}: {e.reason}",
                {"time_ms": 0.0, "size_bytes": 0},
            )
    except urllib.error.URLError as e:
        return (
            None,
            0,
            f"Connection error: {e.reason}",
            {"time_ms": 0.0, "size_bytes": 0},
        )
    except Exception as e:
        return None, 0, f"Unexpected error: {e}", {"time_ms": 0.0, "size_bytes": 0}


def deep_diff(
    expected: Any, actual: Any, path: str = "", ignore_paths: Optional[List[str]] = None
) -> List[str]:
    ignore_paths = ignore_paths or []
    for p in ignore_paths:
        if path.startswith(p):
            return []
    for pat in VOLATILE_PATH_PATTERNS:
        if pat.search(path):
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
        diffs: List[str] = []
        ek, ak = set(expected.keys()), set(actual.keys())
        for k in ek - ak:
            diffs.append(f"{path}.{k}: Missing in actual response")
        for k in ak - ek:
            diffs.append(f"{path}.{k}: Unexpected in actual response")
        for k in ek & ak:
            nxt = f"{path}.{k}" if path else k
            diffs.extend(deep_diff(expected[k], actual[k], nxt, ignore_paths))
        return diffs
    if isinstance(expected, list):
        if len(expected) != len(actual):
            return [
                f"{path}: List length mismatch - expected {len(expected)} but got {len(actual)}"
            ]
        out: List[str] = []
        for i, (e, a) in enumerate(zip(expected, actual)):
            out.extend(deep_diff(e, a, f"{path}[{i}]", ignore_paths))
        return out
    return (
        []
        if expected == actual
        else [f"{path}: Expected {json.dumps(expected)} but got {json.dumps(actual)}"]
    )


# --------------------- File + Result logic ---------------------
def determine_endpoint(file_path: Path) -> str:
    """Determine API endpoint from test file.

    Priority:
    1. Explicit 'endpoint' field in test file (preferred for data endpoints)
    2. Path-based detection for construction/ directory structure
    """
    # Try to read endpoint from test file first
    try:
        test = json.loads(file_path.read_text(encoding="utf-8"))
        if "endpoint" in test:
            return test["endpoint"]
    except Exception:
        pass

    # Fallback: derive from path (construction endpoints)
    try:
        rel = file_path.resolve().relative_to(Path(BASE_PATH).resolve())
        # Handle both old structure (construction/X) and new (construction/X or data/X)
        if rel.parts[0] == "construction" and len(rel.parts) > 1:
            return f"/construction/{rel.parts[1]}"
        return f"/construction/{rel.parts[0]}"
    except Exception:
        parts = list(file_path.resolve().parts)
        for i, p in enumerate(parts):
            if p == "construction" and i + 1 < len(parts):
                return f"/construction/{parts[i + 1]}"
        return f"/construction/{file_path.parent.name}"


def get_ignore_paths(endpoint: str) -> List[str]:
    endpoint_key = endpoint.split("/")[-1]
    return VOLATILE_FIELDS.get(endpoint_key, DATA_ENDPOINT_VOLATILE_FIELDS.get(endpoint_key, []))


def _fmt_bytes(n: Any) -> str:
    try:
        n = int(n)
    except Exception:
        return str(n)
    u = ["B", "KB", "MB", "GB"]
    s = float(n)
    for unit in u:
        if s < 1024.0 or unit == u[-1]:
            return f"{int(s)} {unit}" if unit == "B" else f"{s:.1f} {unit}"
        s /= 1024.0
    return f"{s:.1f} {u[-1]}"  # Fallback return for edge cases


def _fmt_ms(ms: Any) -> str:
    try:
        v = float(ms)
    except Exception:
        return str(ms)
    return f"{v:.0f} ms" if v < 1000 else f"{v / 1000.0:.2f} s"


def _status_text(status: str) -> str:
    return {
        "passed": f"{GREEN}PASSED{RESET}",
        "failed": f"{RED}FAILED{RESET}",
        "skipped": f"{YELLOW}SKIPPED{RESET}",
    }.get(status, f"{MAGENTA}ERROR{RESET}")


def _schema_desc(d: str) -> str:
    return (
        d.split("/")[-1]
        if isinstance(d, str) and d.startswith("#/components/schemas/")
        else d
    )


def _rate_style(pr: float) -> str:
    try:
        v = float(pr)
    except Exception:
        return "green"
    if v >= 90.0:
        return "green"
    if v >= 50.0:
        return "yellow"
    return "red"


def print_result(
    res: Dict[str, Any], verbose: bool = False, rich_collect: bool = False
) -> Optional[Dict[str, str]]:
    s = res["status"]
    color, sym = (
        (GREEN, ".")
        if s == "passed"
        else (
            (RED, "F")
            if s == "failed"
            else ((YELLOW, "S") if s == "skipped" else (MAGENTA, "E"))
        )
    )
    if verbose and rich_collect and USE_RICH and RICH_CONSOLE is not None:
        schema_info = res.get("schema_info", {})
        met = res.get("metrics", {})
        return {
            "file": res.get("file", ""),
            "status": _status_text(s),
            "http": str(met.get("status", "")),
            "time": _fmt_ms(met.get("time_ms", 0)),
            "size": _fmt_bytes(met.get("size_bytes", 0)),
            "schema": _schema_desc(schema_info.get("descriptor", "")),
        }
    if verbose:
        print(f"  {res['file']}: {color}{s.upper()}{RESET}")
        sch = res.get("schema_info", {})
        if sch:
            print(
                f"    - Schema: {_schema_desc(sch.get('descriptor', 'unavailable'))} [{(sch.get('status', 'unavailable') or '').upper()}]"
            )
            if SHOW_SCHEMA_DETAILS and sch.get("summary"):
                print(f"    - Schema details: {sch['summary']}")
        met = res.get("metrics", {})
        if met:
            print(
                f"    - HTTP: {met.get('status', '')} • Time: {_fmt_ms(met.get('time_ms', 0))} • Size: {_fmt_bytes(met.get('size_bytes', 0))}"
            )
        if s == "failed" and res.get("differences"):
            for d in res["differences"]:
                print(f"    - {d}")
        elif s == "error" and res.get("error"):
            print(f"    - {res['error']}")
    else:
        sys.stdout.write(f"{color}{sym}{RESET}")
        sys.stdout.flush()
    return None


def _normalize_req_ids(msg: str) -> str:
    import re

    return re.sub(r"request \d+:", "request [ID]:", msg)


def validate_file(file_path: Path) -> Dict[str, Any]:
    res: Dict[str, Any] = {
        "file": str(file_path.relative_to(BASE_PATH)),
        "status": "unknown",
        "differences": [],
        "error": None,
    }
    try:
        test = json.loads(Path(file_path).read_text(encoding="utf-8"))
        if "skip_reason" in test:
            res.update({"status": "skipped", "error": test["skip_reason"]})
            return res
        if "network" in test and NETWORK_ID and test["network"] != NETWORK_ID:
            res.update({"status": "skipped", "error": f"Requires network '{test['network']}', running on '{NETWORK_ID}'"})
            return res
        if "request_body" not in test:
            res.update({"status": "skipped", "error": "No request_body found"})
            return res
        has_ok, has_err = ("expected_response" in test), ("expected_error" in test)
        if not (has_ok or has_err):
            skip_reason = test.get(
                "description", "No expected_response or expected_error found"
            )
            res.update(
                {
                    "status": "skipped",
                    "error": skip_reason,
                }
            )
            return res

        endpoint = determine_endpoint(file_path)
        actual, http, err, metrics = call_api(endpoint, test["request_body"])
        if err:
            res.update({"status": "error", "error": f"API call failed: {err}"})
            return res

        # Schema
        sch_errs: List[str] = []
        sch_desc, sch_status, sch_summary = "unavailable", "unavailable", None
        if SCHEMA_VALIDATOR is not None and actual is not None:
            try:
                sch_errs = SCHEMA_VALIDATOR.validate_response(endpoint, http, actual)
                sch_desc = SCHEMA_VALIDATOR.describe_response_schema(endpoint, http)
            except Exception as e:
                sch_errs, sch_desc = [f"Schema validator exception: {e}"], "exception"
        if (
            not SCHEMA_VALIDATOR
            or not getattr(SCHEMA_VALIDATOR, "enabled", False)
            or getattr(SCHEMA_VALIDATOR, "spec", None) is None
        ):
            stats["schema_unavailable"] += 1
            sch_status = "unavailable"
        else:
            if sch_desc == "none":
                stats["schema_skipped"] += 1
                sch_status = "skipped"
            else:
                stats["schema_checked"] += 1
                if sch_errs:
                    stats["schema_failed"] += 1
                    sch_status = "failed"
                else:
                    stats["schema_passed"] += 1
                    sch_status = "passed"
        if SHOW_SCHEMA_DETAILS and SCHEMA_VALIDATOR and SCHEMA_VALIDATOR.enabled:
            r = SCHEMA_VALIDATOR.get_resolved_schema(endpoint, http)
            sch_summary = (
                SCHEMA_VALIDATOR.summarize_schema(r) if r is not None else None
            )
        res["schema_info"] = {
            "enabled": bool(
                SCHEMA_VALIDATOR and getattr(SCHEMA_VALIDATOR, "enabled", False)
            ),
            "status": sch_status,
            "endpoint": endpoint,
            "http_status": http,
            "descriptor": sch_desc,
            "errors": sch_errs,
            "summary": sch_summary,
        }
        res["metrics"] = {
            "status": http,
            "time_ms": float((metrics or {}).get("time_ms", 0.0)),
            "size_bytes": int((metrics or {}).get("size_bytes", 0)),
        }

        # Expectations
        if has_ok:
            if http != 200:
                err_obj = (
                    (actual.get("error", actual) if isinstance(actual, dict) else {})
                    if actual
                    else {}
                )
                code = err_obj.get("code", "unknown")
                msg = err_obj.get("message", "unknown message")
                det = err_obj.get("details", {})
                dm = (
                    f" - {det.get('message')}"
                    if isinstance(det, dict) and "message" in det
                    else (f" - {det}" if isinstance(det, str) else "")
                )
                res.update(
                    {
                        "status": "failed",
                        "differences": [
                            f"Expected successful response but got ERROR {code}: {msg}{dm} (HTTP {http})"
                        ],
                    }
                )
            else:
                diffs = deep_diff(
                    test["expected_response"], actual, "", get_ignore_paths(endpoint)
                )
                if sch_errs:
                    diffs.extend([f"Schema: {m}" for m in sch_errs])
                res.update(
                    {"status": "failed", "differences": diffs}
                ) if diffs else res.update({"status": "passed"})
        else:  # has_err
            if http != 200:
                exp = test["expected_error"]
                act_obj: Dict[str, Any] = actual if isinstance(actual, dict) else {}
                act = act_obj.get("error", act_obj)
                if "code" in exp and "code" in act and exp["code"] != act["code"]:
                    res.update(
                        {
                            "status": "failed",
                            "differences": [
                                f"Error code mismatch: expected {exp['code']} but got {act['code']}"
                            ],
                        }
                    )
                else:
                    diffs: List[str] = []
                    if (
                        "message" in exp
                        and "message" in act
                        and exp["message"] != act["message"]
                    ):
                        diffs.append(
                            f'message: Expected "{exp["message"]}" but got "{act["message"]}"'
                        )
                    if (
                        "details" in exp
                        and "details" in act
                        and isinstance(exp["details"], dict)
                        and isinstance(act["details"], dict)
                    ):
                        em, am = (
                            exp["details"].get("message"),
                            act["details"].get("message"),
                        )
                        if (
                            isinstance(em, str)
                            and isinstance(am, str)
                            and _normalize_req_ids(em) != _normalize_req_ids(am)
                        ):
                            diffs.append(
                                f'details.message: Expected "{em}" but got "{am}"'
                            )
                    if sch_errs:
                        diffs.extend([f"Schema: {m}" for m in sch_errs])
                    res.update(
                        {"status": "failed", "differences": diffs}
                    ) if diffs else res.update({"status": "passed"})
            else:
                res.update(
                    {
                        "status": "failed",
                        "differences": ["Expected error but got successful response"],
                    }
                )
    except json.JSONDecodeError as e:
        res.update({"status": "error", "error": f"Invalid JSON in file: {e}"})
    except Exception as e:
        res.update({"status": "error", "error": f"Unexpected error: {e}"})
    return res


# --------------------- Reporting ---------------------
def print_summary(elapsed_time: float = 0) -> None:
    print("\n" + "=" * 80)
    print(f"{BOLD}VALIDATION SUMMARY{RESET}")
    print("=" * 80)
    total = stats["files_tested"]
    passed = stats["files_passed"]
    failed = stats["files_failed"]
    skipped = stats["files_skipped"]
    errors = total - passed - failed - skipped
    rate = (passed / total * 100) if total > 0 else 0
    print(f"\nTotal files found: {stats['total_files']}")
    print(f"Files tested: {total}")
    print(f"  {GREEN}✓ Passed: {passed} ({rate:.1f}%){RESET}")
    print(f"  {RED}✗ Failed: {failed}{RESET}")
    print(f"  {YELLOW}○ Skipped: {skipped}{RESET}")
    print(f"  {MAGENTA}⚠ Errors: {errors}{RESET}")
    print(f"\n{BOLD}Schema validation:{RESET}")
    print(
        f"  checked: {stats['schema_checked']}, {GREEN}passed: {stats['schema_passed']}{RESET}, {RED}failed: {stats['schema_failed']}{RESET}, {YELLOW}skipped: {stats['schema_skipped']}{RESET}, {MAGENTA}unavailable: {stats['schema_unavailable']}{RESET}"
    )
    print(f"\n{BOLD}Per-endpoint status:{RESET}")
    sums = stats.get("endpoint_summaries", [])
    if sums:
        for s in sums:
            tot = s["passed"] + s["failed"] + s["skipped"] + s["errors"]
            print(
                f"  {s['endpoint']}: {GREEN}{s['passed']}{RESET}/{tot} passed, {RED}{s['failed']}{RESET} failed, {YELLOW}{s['skipped']}{RESET} skipped, {MAGENTA}{s['errors']}{RESET} errors"
            )
    if elapsed_time > 0:
        print(f"\n{BOLD}Total execution time:{RESET} {CYAN}{elapsed_time:.2f}s{RESET}")
    print("\n" + "=" * 80)
    print(
        f"{GREEN if failed == 0 and errors == 0 else RED}{BOLD}{'✓ RESULT: PASS' if failed == 0 and errors == 0 else '✗ RESULT: FAIL'}{RESET}"
    )


# --------------------- CLI + Runner ---------------------
def _collect_files(args_paths: List[str]) -> List[Path]:
    if not args_paths:
        return list(Path(BASE_PATH).rglob("*.json"))
    out: List[Path] = []
    for pattern in args_paths:
        if os.path.exists(pattern):
            p = Path(pattern)
            if p.is_file() and p.suffix == ".json":
                out.append(p.resolve())
            elif p.is_dir():
                out.extend(p.rglob("*.json"))
            continue
        out.extend(
            Path(f).resolve()
            for f in glob.glob(pattern, recursive=True)
            if f.endswith(".json")
        )
        base_pat = str(Path(BASE_PATH) / pattern)
        out.extend(
            Path(f) for f in glob.glob(base_pat, recursive=True) if f.endswith(".json")
        )
        if os.path.isdir(Path(BASE_PATH) / pattern):
            out.extend((Path(BASE_PATH) / pattern).rglob("*.json"))
    # de-dup
    return list({p.resolve() for p in out})


def main() -> None:
    start_time = time.time()
    parser = argparse.ArgumentParser(
        description="Validate Rosetta construction test files against a running API",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=(
            "Examples:\n"
            "  python3 test_construction_api.py\n"
            "  python3 test_construction_api.py -u http://localhost:8080 -v\n"
            "  python3 test_construction_api.py 'preprocess/**/*.json'\n"
            "  python3 test_construction_api.py --base-dir /path/to/golden_examples/rosetta_java/construction\n"
            "  python3 test_construction_api.py -j 20  # Run with 20 parallel workers\n"
            "  python3 test_construction_api.py --network-id devkit  # Replace {{networkId}} with 'devkit'\n"
            "  python3 test_construction_api.py -n preprod -v  # Use preprod network with verbose output\n"
        ),
    )
    parser.add_argument(
        "paths",
        nargs="*",
        help="File paths or glob patterns (relative to CWD or --base-dir)",
    )
    parser.add_argument(
        "--base-dir",
        default=str(DEFAULT_BASE_DIR),
        help="Root directory with construction tests",
    )
    parser.add_argument(
        "-u", "--url", default=DEFAULT_ROSETTA_URL, help="Rosetta API base URL"
    )
    parser.add_argument(
        "--openapi",
        default=str(DEFAULT_OPENAPI_PATH),
        help="OpenAPI spec file (YAML/JSON)",
    )
    parser.add_argument(
        "--no-schema", action="store_true", help="Disable schema validation"
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
    args = parser.parse_args()

    global \
        ROSETTA_URL, \
        VERBOSE, \
        BASE_PATH, \
        SCHEMA_VALIDATION_ENABLED, \
        OPENAPI_SPEC_PATH, \
        SCHEMA_VALIDATOR, \
        SHOW_SCHEMA_DETAILS, \
        RICH_CONSOLE, \
        USE_RICH, \
        WORKERS, \
        NETWORK_ID
    ROSETTA_URL = args.url
    VERBOSE = args.verbose
    BASE_PATH = Path(args.base_dir)
    SCHEMA_VALIDATION_ENABLED = not args.no_schema
    OPENAPI_SPEC_PATH = Path(args.openapi) if args.openapi else None
    SHOW_SCHEMA_DETAILS = bool(args.schema_details or args.verbose)
    WORKERS = args.workers
    NETWORK_ID = args.network_id

    original_stdout = sys.stdout
    if args.output:
        sys.stdout = open(args.output, "w", encoding="utf-8")

    try:
        if VERBOSE and Console is not None:
            RICH_CONSOLE = Console()
            USE_RICH = True
        else:
            RICH_CONSOLE = None
            USE_RICH = False

        if SCHEMA_VALIDATION_ENABLED and OPENAPI_SPEC_PATH is not None:
            SCHEMA_VALIDATOR = SchemaValidator(OPENAPI_SPEC_PATH, enabled=True)
        else:
            SCHEMA_VALIDATOR = None

        print("\nScanning for test files...")
        files = _collect_files(args.paths)
        stats["total_files"] = len(files)
        print(f"Found {len(files)} test files\n")

        files_by_endpoint: Dict[str, List[Path]] = defaultdict(list)
        for fp in files:
            files_by_endpoint[determine_endpoint(fp)].append(fp)

        for endpoint in sorted(files_by_endpoint):
            flist = files_by_endpoint[endpoint]
            name = endpoint.split("/")[-1]
            print("")
            print(f"{BOLD}Testing {endpoint} ({len(flist)} files){RESET}")
            print("-" * 40)

            ep_passed = ep_failed = ep_skipped = ep_errors = 0
            ep_failures: List[Tuple[str, str]] = []
            ep_error_cases: List[Tuple[str, str]] = []
            ep_skips: List[Tuple[str, str]] = []
            ep_total_time_ms = 0.0
            ep_total_bytes = 0
            ep_slowest_ms = 0.0
            ep_slowest_file = ""

            groups: Dict[str, List[Path]] = defaultdict(list)
            for fp in sorted(flist):
                try:
                    rel = fp.resolve().relative_to(Path(BASE_PATH).resolve())
                    group = rel.parts[1] if len(rel.parts) > 1 else "_root"
                except Exception:
                    group = fp.parent.name
                groups[group].append(fp)

            collecting = bool(
                VERBOSE
                and USE_RICH
                and RICH_CONSOLE is not None
                and Table is not None
                and Live is not None
            )
            for gname in sorted(groups):
                gfiles = groups[gname]
                if collecting:
                    assert (
                        RICH_CONSOLE is not None
                        and Live is not None
                        and Table is not None
                    )
                    schemas_panel = None
                    if SCHEMA_VALIDATOR and SCHEMA_VALIDATOR.enabled:
                        suc = SCHEMA_VALIDATOR.get_resolved_schema(endpoint, 200)
                        err = SCHEMA_VALIDATOR.get_resolved_schema(endpoint, 500)
                        ssum = SCHEMA_VALIDATOR.summarize_schema(suc) if suc else None
                        esum = SCHEMA_VALIDATOR.summarize_schema(err) if err else None
                        if ssum or esum:
                            st = Table(box=box.SIMPLE if box else None, expand=True)
                            st.add_column(
                                "Success schema", style="green", overflow="fold"
                            )
                            st.add_column(
                                "Error schema", style="yellow", overflow="fold"
                            )
                            st.add_row(ssum or "—", esum or "—")
                            schemas_panel = (
                                Panel(st, title="Schemas", border_style="cyan")
                                if Panel is not None
                                else st
                            )

                    rt = Table.grid(expand=True, padding=(1, 1))
                    rt.add_column("File", ratio=6, overflow="fold")
                    rt.add_column("Status", ratio=2)
                    rt.add_column("HTTP", ratio=1)
                    rt.add_column("Time", ratio=1)
                    rt.add_column("Size", ratio=1)
                    rt.add_column("Schema", ratio=4, overflow="fold")
                    rt.add_row(
                        *(
                            Text(h, style="bold") if Text else h
                            for h in (
                                "File",
                                "Status",
                                "HTTP",
                                "Time",
                                "Size",
                                "Schema",
                            )
                        )
                    )

                    root = (
                        Panel(
                            Group(schemas_panel, rt),
                            title=f"{name}/{gname}",
                            border_style="white",
                            expand=True,
                        )
                        if Panel and Group
                        else rt
                    )
                    with Live(root, console=RICH_CONSOLE, refresh_per_second=8):
                        # Process files in parallel, maintain order for display
                        with ThreadPoolExecutor(max_workers=WORKERS) as executor:
                            future_to_fp = {
                                executor.submit(validate_file, fp): fp for fp in gfiles
                            }
                            results = []
                            for future in as_completed(future_to_fp):
                                fp = future_to_fp[future]
                                r = future.result()
                                results.append((fp, r))

                            # Sort results back to original order for consistent display
                            results.sort(key=lambda x: gfiles.index(x[0]))

                            # Process results in order
                            for fp, r in results:
                                stats["files_tested"] += 1
                                if r["status"] == "passed":
                                    stats["files_passed"] += 1
                                    ep_passed += 1
                                elif r["status"] == "failed":
                                    stats["files_failed"] += 1
                                    stats["errors_by_endpoint"][name] += 1
                                    ep_failed += 1
                                    (
                                        ep_failures.append(
                                            (
                                                r["file"],
                                                (r.get("differences") or [""])[0],
                                            )
                                        )
                                        if r.get("differences")
                                        else None
                                    )
                                elif r["status"] == "skipped":
                                    stats["files_skipped"] += 1
                                    ep_skipped += 1
                                    ep_skips.append(
                                        (r["file"], r.get("error") or "skipped")
                                    )
                                else:
                                    stats["errors_by_endpoint"][name] += 1
                                    ep_errors += 1
                                    ep_error_cases.append(
                                        (r["file"], r.get("error") or "error")
                                    )
                                row = print_result(
                                    r, verbose=VERBOSE, rich_collect=True
                                )
                                if row is not None:
                                    rt.add_row(
                                        fp.name,
                                        row["status"],
                                        row["http"],
                                        row["time"],
                                        row["size"],
                                        row["schema"],
                                    )
                                    m = r.get("metrics") or {}
                                    ms = float(m.get("time_ms", 0.0) or 0.0)
                                    sz = int(m.get("size_bytes", 0) or 0)
                                    ep_total_time_ms += ms
                                    ep_total_bytes += sz
                                    if ms > ep_slowest_ms:
                                        ep_slowest_ms = ms
                                        ep_slowest_file = fp.name
                                # In verbose mode, show details immediately for failures/errors
                                if r["status"] in ("failed", "error"):
                                    if Panel is not None and Text is not None:
                                        if r["status"] == "failed" and r.get(
                                            "differences"
                                        ):
                                            details_text = Text()
                                            for d in r.get("differences", []):
                                                details_text.append(
                                                    f"• {d}\n", style="red"
                                                )
                                            RICH_CONSOLE.print(
                                                Panel(
                                                    details_text,
                                                    title=f"Details: {fp.name}",
                                                    border_style="red",
                                                )
                                            )
                                        elif r["status"] == "error" and r.get("error"):
                                            RICH_CONSOLE.print(
                                                Panel(
                                                    Text(str(r.get("error"))),
                                                    title=f"Error: {fp.name}",
                                                    border_style="magenta",
                                                )
                                            )
                                    else:
                                        # Fallback plain output
                                        if r["status"] == "failed" and r.get(
                                            "differences"
                                        ):
                                            print(f"\n  Details: {fp.name}")
                                            for d in r.get("differences", []):
                                                print(f"    - {d}")
                                        elif r["status"] == "error" and r.get("error"):
                                            print(
                                                f"\n  Error: {fp.name} :: {r.get('error')}"
                                            )
                else:
                    print(f"\n  {DIM}[{name}/{gname}]{RESET}")
                    # Process files in parallel
                    with ThreadPoolExecutor(max_workers=WORKERS) as executor:
                        future_to_fp = {
                            executor.submit(validate_file, fp): fp for fp in gfiles
                        }
                        for future in as_completed(future_to_fp):
                            fp = future_to_fp[future]
                            r = future.result()
                            stats["files_tested"] += 1
                            if r["status"] == "passed":
                                stats["files_passed"] += 1
                                ep_passed += 1
                            elif r["status"] == "failed":
                                stats["files_failed"] += 1
                                stats["errors_by_endpoint"][name] += 1
                                ep_failed += 1
                                (
                                    ep_failures.append(
                                        (r["file"], (r.get("differences") or [""])[0])
                                    )
                                    if r.get("differences")
                                    else None
                                )
                            elif r["status"] == "skipped":
                                stats["files_skipped"] += 1
                                ep_skipped += 1
                                ep_skips.append(
                                    (r["file"], r.get("error") or "skipped")
                                )
                            else:
                                stats["errors_by_endpoint"][name] += 1
                                ep_errors += 1
                                ep_error_cases.append(
                                    (r["file"], r.get("error") or "error")
                                )
                            print_result(r, verbose=VERBOSE)
                            m = r.get("metrics") or {}
                            ms = float(m.get("time_ms", 0.0) or 0.0)
                            sz = int(m.get("size_bytes", 0) or 0)
                            ep_total_time_ms += ms
                            ep_total_bytes += sz
                            if ms > ep_slowest_ms:
                                ep_slowest_ms = ms
                                ep_slowest_file = fp.name

            total_ep = ep_passed + ep_failed + ep_skipped + ep_errors
            pr = (ep_passed / total_ep * 100.0) if total_ep else 0.0
            if VERBOSE and USE_RICH and RICH_CONSOLE is not None and Panel is not None:
                avg_ms = (ep_total_time_ms / total_ep) if total_ep else 0.0
                if Group is not None and Text is not None:
                    # Rich-colored two-line compact summary
                    line1 = Text()
                    line1.append("Files ")
                    line1.append(str(total_ep), style="cyan")
                    line1.append(" • Passed ")
                    line1.append(str(ep_passed), style="green")
                    line1.append(" • Failed ")
                    line1.append(str(ep_failed), style="red")
                    line1.append(" • Skipped ")
                    line1.append(str(ep_skipped), style="yellow")
                    line1.append(" • Errors ")
                    line1.append(str(ep_errors), style="magenta")
                    line1.append(" • Pass ")
                    line1.append(f"{pr:.1f}%", style=_rate_style(pr))

                    line2 = Text()
                    line2.append("Total ")
                    line2.append(_fmt_ms(ep_total_time_ms), style="cyan")
                    line2.append(" • Avg ")
                    line2.append(_fmt_ms(avg_ms), style="cyan")
                    line2.append(" • Data ")
                    line2.append(_fmt_bytes(ep_total_bytes), style="cyan")
                    if ep_slowest_file:
                        line2.append(" • Slowest ")
                        line2.append(ep_slowest_file, style="blue")
                        line2.append(" (")
                        line2.append(_fmt_ms(ep_slowest_ms), style="yellow")
                        line2.append(")")
                    content = Group(line1, line2)
                else:
                    # Fallback to ANSI-colored strings
                    base_line = (
                        f"Files {CYAN}{total_ep}{RESET} • "
                        f"Passed {GREEN}{ep_passed}{RESET} • "
                        f"Failed {RED}{ep_failed}{RESET} • "
                        f"Skipped {YELLOW}{ep_skipped}{RESET} • "
                        f"Errors {MAGENTA}{ep_errors}{RESET} • "
                        f"Pass {GREEN if pr >= 90 else (YELLOW if pr >= 50 else RED)}{pr:.1f}%{RESET}"
                    )
                    metrics_line = (
                        f"Total {CYAN}{_fmt_ms(ep_total_time_ms)}{RESET} • "
                        f"Avg {CYAN}{_fmt_ms(avg_ms)}{RESET} • "
                        f"Data {CYAN}{_fmt_bytes(ep_total_bytes)}{RESET}"
                    )
                    if ep_slowest_file:
                        metrics_line += f" • Slowest {BLUE}{ep_slowest_file}{RESET} ({YELLOW}{_fmt_ms(ep_slowest_ms)}{RESET})"
                    content = base_line + "\n" + metrics_line
                RICH_CONSOLE.print(
                    Panel(
                        content,
                        title=f"\nSummary {endpoint}",
                        border_style="white",
                        expand=True,
                    )
                )
            else:
                print("")
                pr_color = GREEN if pr >= 90 else (YELLOW if pr >= 50 else RED)
                print(
                    "  Summary: "
                    f"{GREEN}{ep_passed}{RESET}/{CYAN}{total_ep}{RESET} passed • "
                    f"{RED}{ep_failed}{RESET} failed • "
                    f"{YELLOW}{ep_skipped}{RESET} skipped • "
                    f"{MAGENTA}{ep_errors}{RESET} errors • "
                    f"pass {pr_color}{pr:.1f}%{RESET}"
                )
                avg_ms = (ep_total_time_ms / total_ep) if total_ep else 0.0
                extra = f"  Time: total {CYAN}{_fmt_ms(ep_total_time_ms)}{RESET} • avg {CYAN}{_fmt_ms(avg_ms)}{RESET} • Data: {CYAN}{_fmt_bytes(ep_total_bytes)}{RESET}"
                if ep_slowest_file:
                    extra += f" • Slowest: {BLUE}{ep_slowest_file}{RESET} ({YELLOW}{_fmt_ms(ep_slowest_ms)}{RESET})"
                print(extra)
            if ep_failed and ep_failures:
                print(f"\n  {BOLD}Failing tests:{RESET}")
                for fn, diff in ep_failures:
                    print(f"    - {BLUE}{fn}{RESET} ::")
                    print(f"      {diff}")
            if ep_errors and ep_error_cases:
                print(f"\n  {BOLD}Errors:{RESET}")
                for fn, reason in ep_error_cases:
                    print(f"    - {BLUE}{fn}{RESET} :: {MAGENTA}{reason}{RESET}")
            if ep_skipped and ep_skips:
                print(f"\n  {BOLD}Skipped tests:{RESET}")
                for fn, reason in ep_skips:
                    print(f"    - {BLUE}{fn}{RESET} :: {YELLOW}{reason}{RESET}")

            stats["endpoint_summaries"].append(
                {
                    "endpoint": endpoint,
                    "passed": ep_passed,
                    "failed": ep_failed,
                    "skipped": ep_skipped,
                    "errors": ep_errors,
                }
            )

        elapsed = time.time() - start_time
        print_summary(elapsed)

        # Exit with proper code based on test results
        failed = stats["files_failed"]
        errors = (
            stats["files_tested"]
            - stats["files_passed"]
            - stats["files_failed"]
            - stats["files_skipped"]
        )
        if failed > 0 or errors > 0:
            sys.exit(1)  # Fail CI when tests fail or have errors
    finally:
        if args.output:
            sys.stdout.close()
            sys.stdout = original_stdout


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print(f"\n{YELLOW}Validation interrupted by user{RESET}")
        print_summary(0)
        sys.exit(1)
    except Exception as e:
        print(f"\n{RED}Fatal error: {e}{RESET}")
        sys.exit(1)
