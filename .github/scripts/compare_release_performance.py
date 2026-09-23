#!/usr/bin/env python3
"""Compare a candidate CSV with a compatible performance table published under docs."""

from __future__ import annotations

import argparse
import csv
import math
import re
from pathlib import Path


EXPECTED_ENDPOINT_COUNT = 8
METRIC_FIELDS = {
    "ID",
    "Endpoint",
    "Max_Concurrency",
    "p95(ms)",
    "p99(ms)",
    "Non_2xx_Responses",
    "Error_Rate(%)",
    "Requests_per_sec",
}
PUBLISHED_HEADERS = {
    "ID": "ID",
    "Endpoint": "Endpoint",
    "Max Concurrency": "Max_Concurrency",
    "p95 (ms)": "p95(ms)",
    "p99 (ms)": "p99(ms)",
    "Non-2xx": "Non_2xx_Responses",
    "Error Rate (%)": "Error_Rate(%)",
    "Reqs/sec": "Requests_per_sec",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--test-results-root", type=Path, required=True)
    parser.add_argument("--candidate-tag", required=True)
    parser.add_argument("--documentation-name", required=True)
    parser.add_argument("--expected-hardware-profile", required=True)
    parser.add_argument("--candidate-dir", type=Path)
    parser.add_argument("--deployment")
    parser.add_argument("--check-baseline-only", action="store_true")
    return parser.parse_args()


DECIMAL = re.compile(r"(?:0|[1-9][0-9]*)(?:\.[0-9]+)?")
INTEGER = re.compile(r"(?:0|[1-9][0-9]*)")


def decimal(value: str, field: str, suffix: str = "") -> float:
    pattern = rf"{DECIMAL.pattern}{re.escape(suffix)}"
    if not isinstance(value, str) or re.fullmatch(pattern, value) is None:
        raise ValueError(f"Invalid {field} value: {value!r}")
    parsed = float(value.removesuffix(suffix) if suffix else value)
    if not math.isfinite(parsed):
        raise ValueError(f"Expected a finite {field}, found {value}")
    return parsed


def integer(value: str, field: str) -> int:
    if not isinstance(value, str) or INTEGER.fullmatch(value) is None:
        raise ValueError(f"Invalid {field} value: {value!r}")
    return int(value)


def endpoint_labels(rows: list[dict[str, str]]) -> list[str]:
    search_occurrence = 0
    labels: list[str] = []
    for row in rows:
        endpoint = row["Endpoint"]
        if endpoint == "/search/transactions":
            search_occurrence += 1
            suffix = "by hash" if search_occurrence == 1 else "by address"
            endpoint = f"{endpoint} ({suffix})"
        labels.append(endpoint)
    return labels


def validate_metrics(
    rows: list[dict[str, str]],
    source: Path,
    latency_suffix: str,
) -> None:
    if len(rows) != EXPECTED_ENDPOINT_COUNT:
        raise ValueError(
            f"Expected {EXPECTED_ENDPOINT_COUNT} endpoints in {source}, found {len(rows)}"
        )
    missing = METRIC_FIELDS.difference(rows[0])
    if missing:
        raise ValueError(f"Missing columns in {source}: {', '.join(sorted(missing))}")

    labels = endpoint_labels(rows)
    if len(set(labels)) != len(labels):
        raise ValueError(f"Duplicate endpoint labels in {source}")

    expected_ids = [str(index) for index in range(1, EXPECTED_ENDPOINT_COUNT + 1)]
    actual_ids = [row["ID"] for row in rows]
    if actual_ids != expected_ids:
        raise ValueError(f"Expected endpoint IDs {expected_ids} in {source}, found {actual_ids}")

    for row in rows:
        endpoint = row["Endpoint"]
        max_concurrency = integer(row["Max_Concurrency"], "max concurrency")
        p95 = decimal(row["p95(ms)"], "p95 latency", latency_suffix)
        p99 = decimal(row["p99(ms)"], "p99 latency", latency_suffix)
        non_2xx = integer(row["Non_2xx_Responses"], "non-2xx response count")
        error_rate = decimal(row["Error_Rate(%)"], "error rate", "%")
        requests_per_sec = decimal(row["Requests_per_sec"], "requests per second")
        if max_concurrency <= 0:
            raise ValueError(f"No passing concurrency recorded for {endpoint} in {source}")
        if p95 < 0 or p99 < 0 or p99 < p95:
            raise ValueError(f"Invalid latency percentiles for {endpoint} in {source}")
        if non_2xx < 0:
            raise ValueError(f"Invalid non-2xx count for {endpoint} in {source}")
        if not 0 <= error_rate <= 100:
            raise ValueError(f"Invalid error rate for {endpoint} in {source}")
        if requests_per_sec <= 0:
            raise ValueError(f"No throughput recorded for {endpoint} in {source}")


def read_candidate_csv(
    path: Path,
    expected_release: str,
    expected_hardware_profile: str | None,
) -> list[dict[str, str]]:
    with path.open(newline="", encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        # Read the header separately, so a run that aborted after writing only
        # the header reports what is wrong instead of raising IndexError.
        fieldnames = reader.fieldnames or []
        rows = list(reader)

    required = METRIC_FIELDS | {"Release", "Hardware_Profile", "Machine_Specs"}
    missing = required.difference(fieldnames)
    if missing:
        raise ValueError(f"Missing columns in {path}: {', '.join(sorted(missing))}")

    if not rows:
        raise ValueError(f"No result rows in {path}.")

    releases = {row["Release"] for row in rows}
    if releases != {expected_release}:
        raise ValueError(
            f"Expected release {expected_release} in {path}, found {', '.join(sorted(releases))}"
        )

    profiles = {row["Hardware_Profile"] for row in rows}
    if len(profiles) != 1:
        raise ValueError(f"Mixed hardware profiles in {path}: {', '.join(sorted(profiles))}")
    if expected_hardware_profile is not None and profiles != {expected_hardware_profile}:
        raise ValueError(
            f"Expected hardware profile {expected_hardware_profile} in {path}, "
            f"found {', '.join(sorted(profiles))}"
        )

    machine_specs = {row["Machine_Specs"] for row in rows}
    if len(machine_specs) != 1:
        raise ValueError(f"Mixed machine specs in {path}: {', '.join(sorted(machine_specs))}")
    if not next(iter(machine_specs)).strip():
        raise ValueError(f"Machine specs are empty in {path}")

    validate_metrics(rows, path, "ms")
    return rows


def markdown_cells(line: str) -> list[str]:
    stripped = line.strip()
    if not stripped.startswith("|") or not stripped.endswith("|"):
        return []
    return [cell.strip() for cell in stripped[1:-1].split("|")]


def published_machine(path: Path) -> str:
    prefix = "- **Machine Specs:**"
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith(prefix) and line.removeprefix(prefix).strip():
            return line.removeprefix(prefix).strip()
    return "not recorded in this published result"


def read_published_doc(path: Path) -> list[dict[str, str]]:
    lines = path.read_text(encoding="utf-8").splitlines()
    for index, line in enumerate(lines):
        headers = markdown_cells(line)
        if headers != list(PUBLISHED_HEADERS):
            continue
        if index + 1 >= len(lines):
            break

        rows: list[dict[str, str]] = []
        for row_line in lines[index + 2 :]:
            cells = markdown_cells(row_line)
            if not cells:
                if rows:
                    break
                continue
            if len(cells) != len(headers):
                raise ValueError(f"Invalid published table row in {path}: {row_line}")
            rows.append(
                {
                    PUBLISHED_HEADERS[header]: value
                    for header, value in zip(headers, cells, strict=True)
                }
            )
        validate_metrics(rows, path, "")
        return rows

    raise ValueError(f"Published performance table not found in {path}")


def delta(previous: float, current: float) -> str:
    if previous == 0:
        return "n/a"
    return f"{((current - previous) / previous) * 100:+.1f}%"


def doc_table(rows: list[dict[str, str]]) -> str:
    labels = endpoint_labels(rows)
    lines = [
        f"- **Hardware Profile:** {rows[0]['Hardware_Profile']}",
        f"- **Machine Specs:** {rows[0]['Machine_Specs']}",
        "",
        "The performance metrics in this table were measured against an SLA of 1000 ms.",
        "",
        "| ID | Endpoint | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |",
        "|---:|---|---:|---:|---:|---:|---:|---:|",
    ]
    for row, label in zip(rows, labels, strict=True):
        lines.append(
            "| {id} | {endpoint} | {max_concurrency} | {p95} | {p99} | "
            "{non_2xx} | {error_rate} | {requests_per_sec} |".format(
                id=row["ID"],
                endpoint=label,
                max_concurrency=row["Max_Concurrency"],
                p95=row["p95(ms)"].removesuffix("ms"),
                p99=row["p99(ms)"].removesuffix("ms"),
                non_2xx=row["Non_2xx_Responses"],
                error_rate=row["Error_Rate(%)"],
                requests_per_sec=row["Requests_per_sec"],
            )
        )
    return "\n".join(lines) + "\n"


def comparison_table(
    baseline: list[dict[str, str]],
    candidate: list[dict[str, str]],
    baseline_tag: str,
    deployment: str,
    baseline_path: Path,
    baseline_machine: str,
) -> str:
    baseline_labels = endpoint_labels(baseline)
    candidate_labels = endpoint_labels(candidate)
    if baseline_labels != candidate_labels:
        raise ValueError("Published baseline and candidate endpoint order differs")

    lines = [
        f"# Performance comparison: {deployment}",
        "",
        f"- Published baseline: `{baseline_tag}`",
        f"- Candidate: `{candidate[0]['Release']}`",
        f"- Published baseline file: `{baseline_path}`",
        f"- Published baseline profile: `{baseline_path.parent.name}`",
        f"- Published baseline machine: `{baseline_machine}`",
        f"- Candidate profile: `{candidate[0]['Hardware_Profile']}`",
        f"- Candidate machine: `{candidate[0]['Machine_Specs']}`",
        "- Decision: manual review required",
        "",
        "Positive latency deltas are slower. Positive concurrency and throughput deltas are higher.",
        "",
        "| Endpoint | Baseline max | Candidate max | Max delta | Baseline p95 | Candidate p95 | p95 delta | Baseline p99 | Candidate p99 | p99 delta | Baseline req/s | Candidate req/s | Req/s delta |",
        "|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|",
    ]

    for old, new, label in zip(baseline, candidate, baseline_labels, strict=True):
        old_max = integer(old["Max_Concurrency"], "published max concurrency")
        new_max = integer(new["Max_Concurrency"], "candidate max concurrency")
        old_p95 = decimal(old["p95(ms)"], "published p95 latency")
        new_p95 = decimal(new["p95(ms)"], "candidate p95 latency", "ms")
        old_p99 = decimal(old["p99(ms)"], "published p99 latency")
        new_p99 = decimal(new["p99(ms)"], "candidate p99 latency", "ms")
        old_rps = decimal(old["Requests_per_sec"], "published requests per second")
        new_rps = decimal(new["Requests_per_sec"], "candidate requests per second")
        lines.append(
            f"| {label} | {old_max:g} | {new_max:g} | {delta(old_max, new_max)} | "
            f"{old_p95:g} | {new_p95:g} | {delta(old_p95, new_p95)} | "
            f"{old_p99:g} | {new_p99:g} | {delta(old_p99, new_p99)} | "
            f"{old_rps:g} | {new_rps:g} | {delta(old_rps, new_rps)} |"
        )

    lines.extend(
        [
            "",
            "## Reliability",
            "",
            "| Endpoint | Baseline non-2xx | Candidate non-2xx | Baseline error rate | Candidate error rate |",
            "|---|---:|---:|---:|---:|",
        ]
    )
    for old, new, label in zip(baseline, candidate, baseline_labels, strict=True):
        lines.append(
            f"| {label} | {old['Non_2xx_Responses']} | {new['Non_2xx_Responses']} | "
            f"{old['Error_Rate(%)']} | {new['Error_Rate(%)']} |"
        )

    return "\n".join(lines) + "\n"


def missing_baseline_report(
    candidate: list[dict[str, str]],
    deployment: str,
    test_results_root: Path,
) -> str:
    return "\n".join(
        [
            f"# Performance comparison: {deployment}",
            "",
            "- Published baseline: none",
            f"- Candidate: `{candidate[0]['Release']}`",
            f"- Test results root: `{test_results_root}`",
            f"- Candidate profile: `{candidate[0]['Hardware_Profile']}`",
            f"- Candidate machine: `{candidate[0]['Machine_Specs']}`",
            "- Decision: first publication",
            "",
            "## First publication",
            "",
            "No earlier result has been published for this deployment and hardware profile.",
            "The candidate result is valid as that profile's first publication without a historical comparison.",
            "",
        ]
    )


def validate_documentation_name(name: str) -> str:
    path = Path(name)
    if path.name != name or path.suffix != ".md":
        raise ValueError("--documentation-name must be a Markdown file name")
    return name


SEMVER = re.compile(r"^(\d+)\.(\d+)\.(\d+)$")


def version_tuple(tag: str) -> tuple[int, int, int]:
    source_tag = tag.removesuffix("-pre-release")
    match = SEMVER.fullmatch(source_tag)
    if match is None:
        raise ValueError(f"Expected a semantic release tag, found {tag}")
    return tuple(int(part) for part in match.groups())


def discover_baseline(
    test_results_root: Path,
    candidate_tag: str,
    hardware_profile: str,
    documentation_name: str,
) -> tuple[str, Path] | None:
    candidate_version = version_tuple(candidate_tag)
    candidates: list[tuple[tuple[int, int, int], str, Path]] = []
    for path in test_results_root.glob(f"*/{hardware_profile}/{documentation_name}"):
        match = SEMVER.fullmatch(path.parents[1].name)
        if match is None or not path.is_file():
            continue
        version = tuple(int(part) for part in match.groups())
        if version[0] == candidate_version[0] and version < candidate_version:
            candidates.append((version, path.parents[1].name, path))

    if not candidates:
        return None
    _, baseline_tag, baseline_path = max(candidates, key=lambda item: item[0])
    return baseline_tag, baseline_path


def main() -> None:
    args = parse_args()
    test_results_root = args.test_results_root.resolve()
    if not test_results_root.is_dir():
        raise FileNotFoundError(f"Test results root unavailable: {test_results_root}")

    documentation_name = validate_documentation_name(args.documentation_name)
    baseline_info = discover_baseline(
        test_results_root,
        args.candidate_tag,
        args.expected_hardware_profile,
        documentation_name,
    )
    if args.check_baseline_only:
        if baseline_info is None:
            print(
                "::warning::No earlier published result; "
                f"{documentation_name} will be a first publication."
            )
            return
        baseline_tag, baseline_doc = baseline_info
        read_published_doc(baseline_doc)
        print(f"Published baseline {baseline_tag}: {baseline_doc}")
        return

    if args.candidate_dir is None or args.deployment is None:
        raise SystemExit("--candidate-dir and --deployment are required for comparison")

    candidate_dir = args.candidate_dir.resolve()
    candidate_csv = candidate_dir / "summary_results.csv"
    candidate = read_candidate_csv(
        candidate_csv,
        args.candidate_tag,
        args.expected_hardware_profile,
    )

    candidate_doc = candidate_dir / documentation_name
    candidate_doc.write_text(doc_table(candidate), encoding="utf-8")

    comparison = candidate_dir / "performance-comparison.md"
    if baseline_info is None:
        report = missing_baseline_report(candidate, args.deployment, test_results_root)
        print(f"::warning::First publication for {documentation_name}")
    else:
        baseline_tag, baseline_doc = baseline_info
        baseline = read_published_doc(baseline_doc)
        baseline_machine = published_machine(baseline_doc)
        report = comparison_table(
            baseline,
            candidate,
            baseline_tag,
            args.deployment,
            baseline_doc,
            baseline_machine,
        )
        print(f"Published baseline {baseline_tag}: {baseline_doc}")
    comparison.write_text(report, encoding="utf-8")

    print(f"Candidate: {candidate_csv}")
    print(f"Documentation table: {candidate_doc}")
    print(f"Comparison: {comparison}")
    print()
    print(report)


if __name__ == "__main__":
    main()
