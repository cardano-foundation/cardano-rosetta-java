"""
Shared fixtures for data endpoint tests.
"""

import ast
import inspect
import json
import os
import subprocess
import textwrap
import pytest
import allure
from pathlib import Path
from dotenv import find_dotenv, load_dotenv
from client import RosettaClient


# Load nearest .env walking up from this file's directory.
# CI creates tests/data-endpoints/.env; locally falls back to repo root .env.
load_dotenv(find_dotenv(), override=True)


def pytest_sessionstart(session):
    """Write Allure environment.properties at the start of the test session."""
    allure_dir = session.config.option.allure_report_dir
    if not allure_dir:
        return

    repo_root = Path(__file__).parent.parent.parent

    def _git(cmd):
        try:
            return subprocess.check_output(
                ["git", "-C", str(repo_root)] + cmd,
                text=True, stderr=subprocess.DEVNULL,
            ).strip()
        except Exception:
            return "unknown"

    env = {
        "Release.Version": _git(["describe", "--tags", "--always"]),
        "Git.Branch": _git(["rev-parse", "--abbrev-ref", "HEAD"]),
        "Git.Commit": _git(["rev-parse", "--short", "HEAD"]),
        "CARDANO_NETWORK": os.environ.get("CARDANO_NETWORK", ""),
        "ROSETTA_URL": os.environ.get("ROSETTA_URL", ""),
        "REMOVE_SPENT_UTXOS": os.environ.get("REMOVE_SPENT_UTXOS", ""),
        "REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT": os.environ.get("REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT", ""),
        "TOKEN_REGISTRY_ENABLED": os.environ.get("TOKEN_REGISTRY_ENABLED", ""),
        "TOKEN_REGISTRY_BASE_URL": os.environ.get("TOKEN_REGISTRY_BASE_URL", ""),
        "PEER_DISCOVERY": os.environ.get("PEER_DISCOVERY", ""),
    }

    props_path = Path(allure_dir) / "environment.properties"
    props_path.parent.mkdir(parents=True, exist_ok=True)
    with open(props_path, "w") as f:
        for key, value in env.items():
            f.write(f"{key}={value}\n")


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    """Attach request/response bodies and assertions to Allure report."""
    outcome = yield
    report = outcome.get_result()
    if report.when == "call":
        for i, (req, resp) in enumerate(getattr(item, "_recorded_responses", [])):
            allure.attach(
                body=json.dumps(req, indent=2, default=str),
                name=f"Request #{i + 1} - {req.get('url', '')}",
                attachment_type=allure.attachment_type.JSON,
            )
            allure.attach(
                body=json.dumps(resp, indent=2, default=str),
                name=f"Response #{i + 1} - HTTP {resp.get('status_code', '?')}",
                attachment_type=allure.attachment_type.JSON,
            )
        try:
            source = textwrap.dedent(inspect.getsource(item.obj))
            lines = source.splitlines()
            tree = ast.parse(source)
            assert_lines = sorted({
                node.lineno for node in ast.walk(tree) if isinstance(node, ast.Assert)
            })
            if assert_lines:
                allure.attach(
                    body="\n".join(lines[n - 1] for n in assert_lines),
                    name="Assertions",
                    attachment_type=allure.attachment_type.TEXT,
                )
        except Exception:
            pass


@pytest.fixture(scope="session")
def rosetta_url():
    """Base URL for Rosetta API."""
    return os.environ.get("ROSETTA_URL", "http://localhost:8082")


@pytest.fixture(scope="session")
def network():
    """Configured Cardano network (preprod, mainnet, preview, etc)."""
    network = os.environ.get("CARDANO_NETWORK")
    if not network:
        raise ValueError(
            "CARDANO_NETWORK environment variable is required. "
            "Set it to 'preprod', 'mainnet', 'preview', etc."
        )
    return network


@pytest.fixture
def client(rosetta_url, network, request):
    """Rosetta API client instance with configured network."""
    with RosettaClient(base_url=rosetta_url, default_network=network) as c:
        original_post = c._post
        request.node._recorded_responses = []

        def _recording_post(path, body, schema_name=None):
            response = original_post(path, body, schema_name=schema_name)
            try:
                response_body = response.json()
            except Exception:
                response_body = response.text
            request.node._recorded_responses.append((
                {"method": "POST", "url": f"{c.base_url}{path}", "body": body},
                {"status_code": response.status_code, "body": response_body},
            ))
            return response

        c._post = _recording_post
        yield c


@pytest.fixture(scope="module")
def blockchain_height(network_status):
    """
    Get current blockchain height once per test module.

    Cached to avoid repeated network_status calls.
    Fails loudly if blockchain is too young for integration testing.
    """
    height = network_status["current_block_identifier"]["index"]

    if height < 100:
        raise AssertionError(
            f"Blockchain too young ({height} blocks). "
            f"Need at least 100 blocks for integration testing."
        )

    return height


@pytest.fixture(scope="session")
def network_status(rosetta_url, network):
    """
    Get network status once per test session.

    Cached to avoid repeated calls and used for configuration detection.
    """
    with RosettaClient(base_url=rosetta_url, default_network=network) as client:
        return client.network_status().json()


@pytest.fixture(scope="session")
def pruning_enabled():
    """Read REMOVE_SPENT_UTXOS from environment."""
    return os.environ.get("REMOVE_SPENT_UTXOS", "false").lower() == "true"


@pytest.fixture(scope="session")
def grace_window():
    """Read pruning grace window from environment."""
    return int(os.environ.get("REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT", "2160"))


@pytest.fixture(scope="session")
def is_pruned_instance(pruning_enabled):
    """
    Check if running against a pruned instance.

    Reads from environment configuration instead of API detection.
    """
    return pruning_enabled


@pytest.fixture(scope="session")
def oldest_block_identifier(network_status, is_pruned_instance):
    """
    Get oldest fully queryable block if pruning is enabled.

    Returns None for non-pruned instances.
    Below this block index, blocks might have missing data due to pruning.

    NOTE: This reads from API response, not configuration.
    Use this to validate the API behavior, not to detect pruning.
    """
    if is_pruned_instance:
        return network_status.get("oldest_block_identifier", {}).get("index")
    return None


@pytest.fixture(scope="session")
def has_token_registry():
    """Read TOKEN_REGISTRY_ENABLED from environment."""
    return os.environ.get("TOKEN_REGISTRY_ENABLED", "false").lower() == "true"


@pytest.fixture(scope="session")
def has_peer_discovery():
    """Read PEER_DISCOVERY from environment."""
    return os.environ.get("PEER_DISCOVERY", "false").lower() == "true"


@pytest.fixture(scope="session")
def network_data(network):
    """
    Load network-specific test data from YAML.

    Returns test addresses, assets, and other network-specific data.
    Fails loudly if network has no test data configured.
    """
    import yaml
    from pathlib import Path

    config_file = Path(__file__).parent / "network_test_data.yaml"

    if not config_file.exists():
        raise FileNotFoundError(
            f"Network test data file not found: {config_file}\n"
            f"Create this file with test data for network '{network}'"
        )

    with open(config_file) as f:
        all_data = yaml.safe_load(f)

    if network not in all_data:
        available = ", ".join(all_data.keys())
        raise ValueError(
            f"No test data configured for network '{network}'.\n"
            f"Available networks: {available}\n"
            f"Add '{network}' section to {config_file}"
        )

    return all_data[network]



