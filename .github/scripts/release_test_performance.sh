#!/usr/bin/env bash
# Release-owned harness and data; reporting tool from the immutable automation checkout.

run_release_stability() (
  local endpoint=$1 tests_dir="$DEPLOY_DIR/tests/legacy-load-tests" run_log result_dir test_status
  run_log="$tests_dir/release-test-${GITHUB_RUN_ID}-${GITHUB_RUN_ATTEMPT}.log"
  test -w "$tests_dir"
  cd "$tests_dir"
  set +e
  python3 stability_test.py \
    --url="$endpoint" \
    --csv=data/mainnet-data.csv \
    --network=mainnet \
    --release="$PRERELEASE_TAG" \
    --hardware-profile=mid_profile \
    --machine-specs="$MACHINE_SPECS" \
    --duration=60 \
    --cooldown=60 \
    --sla=1000 \
    --error-threshold=1.0 \
    --verbose | tee "$run_log"
  test_status=${PIPESTATUS[0]}
  set -e

  result_dir=$(python3 - "$run_log" <<'PY'
import pathlib
import sys

for line in pathlib.Path(sys.argv[1]).read_text().splitlines():
    prefix = "Creating output directory: "
    if line.startswith(prefix):
        print(line.removeprefix(prefix))
        break
PY
  )
  if [[ -z "$result_dir" ]]; then
    echo "Could not determine stability_test.py output directory." >&2
    result_dir="$PWD/failed-run-$GITHUB_RUN_ID"
    mkdir -p "$result_dir"
    mv "$run_log" "$result_dir/github-actions.log"
    echo "result_dir=$result_dir" >> "$GITHUB_OUTPUT"
    exit 1
  fi
  mv "$run_log" "$result_dir/github-actions.log"
  echo "result_dir=$result_dir" >> "$GITHUB_OUTPUT"
  (( test_status == 0 )) || exit "$test_status"
  test -f "$result_dir/summary_results.csv"
)

compare_release_performance() {
  local documentation_name=$1
  shift
  python3 "$RELEASE_SCRIPTS/compare_release_performance.py" \
    --test-results-root "$DEPLOY_DIR/docs/docs/development/test-results" \
    --candidate-tag "$PRERELEASE_TAG" \
    --documentation-name "$documentation_name" \
    --expected-hardware-profile mid_profile "$@"
}
