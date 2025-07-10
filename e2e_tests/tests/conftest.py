import os
import pytest
import logging
import sys # Added for stdout access
import time # Added for timing hooks
import _pytest.terminal # Added for hook overrides
import re # Added for Tee class
from dotenv import load_dotenv

from e2e_tests.rosetta_client.client import RosettaClient
from e2e_tests.wallet_utils.pycardano_wallet import PyCardanoWallet
from e2e_tests.test_utils.transaction_orchestrator import TransactionOrchestrator
from e2e_tests.test_utils.signing_handler import SigningHandler
from e2e_tests.test_utils.utxo_selector import UtxoSelector
from e2e_tests.test_utils.log_formatter import SwissDesignFormatter, Style # Import Style too

# Load environment variables
load_dotenv()

# Configure logging with the custom formatter
# Get the root logger
root_logger = logging.getLogger()
root_logger.setLevel(logging.DEBUG) # Set the root level

# Remove existing handlers (like the default basicConfig handler)
for handler in root_logger.handlers[:]:
    root_logger.removeHandler(handler)

# Create a handler for stdout with the custom formatter
stream_handler = logging.StreamHandler(sys.stdout)
stream_handler.setLevel(logging.DEBUG) # Process all levels
# Explicitly define date format for the handler/formatter
date_format = "%H:%M:%S" 
formatter = SwissDesignFormatter(datefmt=date_format) # Pass datefmt here
stream_handler.setFormatter(formatter)

# Add the new handler to the root logger
root_logger.addHandler(stream_handler)

# Ensure HTTP request logging is properly configured
http_logger = logging.getLogger("http")
http_logger.setLevel(logging.INFO) # Show HTTP requests at INFO level
http_logger.propagate = True  # Ensure log messages propagate to the root handler

# Ensure all logs related to rosetta client are properly tagged
rosetta_logger = logging.getLogger("e2e_tests.rosetta_client")
rosetta_logger.setLevel(logging.DEBUG)

# Ensure test logs are properly tagged
test_logger = logging.getLogger("test")
test_logger.setLevel(logging.INFO)

# Set lower levels for some noisy modules
logging.getLogger("urllib3").setLevel(logging.WARNING)
logging.getLogger("requests").setLevel(logging.WARNING)
logging.getLogger("pycardano").setLevel(logging.WARNING)
# Add other noisy loggers if needed
# logging.getLogger("another_library").setLevel(logging.INFO)


logger = logging.getLogger("test") # Logger for test-specific messages


@pytest.fixture(scope="session")
def rosetta_endpoint():
    """Get the Rosetta API endpoint from environment variables."""
    endpoint = os.getenv("ROSETTA_ENDPOINT", "http://localhost:8082")
    logger.info(f"Using Rosetta endpoint: {endpoint}")
    return endpoint


@pytest.fixture(scope="session")
def cardano_network():
    """Get the Cardano network from environment variables."""
    network = os.getenv("CARDANO_NETWORK", "preprod") # Default to preprod instead of testnet
    logger.info(f"Using Cardano network: {network}")
    return network


@pytest.fixture(scope="session")
def rosetta_client(rosetta_endpoint, cardano_network):
    """Create a RosettaClient instance."""
    client = RosettaClient(endpoint=rosetta_endpoint, network=cardano_network)
    # Test connection
    try:
        # Verify /network/list endpoint returns the desired network
        logger.debug("Verifying /network/list endpoint...")
        networks_list = client.network_list()
        network_identifiers = networks_list.get("network_identifiers", [])
        networks = [n.get("network") for n in network_identifiers if n.get("blockchain") == "cardano"]
        assert cardano_network in networks, f"Expected network {cardano_network} not found in /network/list response: {networks}"
        logger.debug(f"Network list verified successfully: {networks}")
        
        # Verify /network/options endpoint returns operations we need
        logger.debug("Verifying /network/options endpoint...")
        options = client.network_options()
        
        # Check for operations in the standard path
        if "allow" in options and "operation_types" in options["allow"]:
            op_types = options["allow"]["operation_types"]
            
            # Check for essential operations
            required_operations = ["input", "output", "stakeKeyRegistration", "stakeDelegation", "stakeKeyDeregistration", "dRepVoteDelegation"]
            missing_ops = [op for op in required_operations if op not in op_types]
            if missing_ops:
                raise AssertionError(f"Required operations missing: {missing_ops}")
            logger.debug(f"Operation types verified successfully. Found {len(op_types)} operation types")
        else:
            logger.warning("Could not find operation_types in network/options response. Continuing without validation.")
        
        # Verify /network/status endpoint works
        status = client.network_status()
        current_block = status.get("current_block_identifier", {})
        block_index = current_block.get("index", "unknown")
        block_hash = current_block.get("hash", "unknown")
        logger.info(f"Connected to Rosetta API · Current block: {block_index} · Hash: {block_hash}")
    except Exception as e:
        logger.error(f"‼ Failed to connect to Rosetta API: {str(e)}")
        raise
    return client


@pytest.fixture(scope="session")
def test_wallet(cardano_network):
    """Create a PyCardanoWallet instance from mnemonic in environment variables."""
    mnemonic = os.getenv("TEST_WALLET_MNEMONIC")
    if not mnemonic:
        raise ValueError("TEST_WALLET_MNEMONIC environment variable is required")
    
    # Use the cardano_network fixture to initialize the wallet correctly
    wallet = PyCardanoWallet.from_mnemonic(mnemonic=mnemonic, network=cardano_network) 
    
    address = wallet.get_address()
    logger.info(f"Test wallet initialized · Address: {address}")
    
    return wallet


@pytest.fixture(scope="session")
def transaction_orchestrator(rosetta_client):
    """Create a TransactionOrchestrator instance."""
    return TransactionOrchestrator(client=rosetta_client)


@pytest.fixture(scope="session")
def signing_handler(test_wallet):
    """Create a SigningHandler instance."""
    return SigningHandler(wallet=test_wallet)


@pytest.fixture(scope="session")
def utxo_selector():
    """Create a UtxoSelector instance."""
    return UtxoSelector()


@pytest.fixture(scope="session")
def stake_pool_hash():
    """Get the stake pool HASH from environment variables."""
    pool_hash = os.getenv("STAKE_POOL_HASH")
    if not pool_hash:
        pytest.skip("STAKE_POOL_HASH environment variable is required for stake delegation tests")
    logger.info(f"Using Stake Pool Hash: {pool_hash}")
    return pool_hash


@pytest.fixture(scope="session")
def drep_key_hash_id():
    """Get the DRep key hash ID from environment variables."""
    drep_id = os.getenv("DREP_KEY_HASH_ID")
    if not drep_id:
        pytest.skip("DREP_KEY_HASH_ID environment variable is required for vote delegation tests")
    return drep_id


@pytest.fixture(scope="session")
def drep_script_hash_id():
    """Get the DRep script hash ID from environment variables."""
    drep_id = os.getenv("DREP_SCRIPT_HASH_ID")
    if not drep_id:
        pytest.skip("DREP_SCRIPT_HASH_ID environment variable is required for vote delegation tests")
    return drep_id


@pytest.fixture(scope="session")
def pool_governance_proposal_id():
    """Get the governance proposal ID from environment variables."""
    proposal_id = os.environ.get("POOL_GOVERNANCE_PROPOSAL_ID")
    if not proposal_id:
        pytest.skip("POOL_GOVERNANCE_PROPOSAL_ID environment variable is required for pool governance vote tests")
    return proposal_id


@pytest.fixture(scope="session")
def pool_registration_cert():
    """Get the pool registration certificate (hex) from environment variables."""
    cert = os.environ.get("POOL_REGISTRATION_CERT")
    if not cert:
        pytest.skip("POOL_REGISTRATION_CERT environment variable is required for poolRegistrationWithCert test")
    return cert


# --- Pytest Hooks for Custom Reporting --- 

# Store collected tests for accurate reporting
collected_tests = {"items": [], "nodeids": []}
# Track completed test results by nodeid
completed_tests = {}
# Track which tests have already been reported in output
reported_tests = set()
# Track session start time for accurate duration reporting
session_start_time = None


# Core status reporting hook - completely eliminates 'F' and 's' markers
@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_protocol(item, nextitem):
    """Completely disable the default status reporting in the terminal"""
    # Run the original hook
    yield

    # We've already run the test, but we don't want pytest to print
    # any status markers like 'F' or 's' after our output


# This is the most important hook to suppress the F and s markers
@pytest.hookimpl(tryfirst=True)
def pytest_report_teststatus(report, config):
    """
    Disable default status markers (F, s, etc.) completely by returning
    a tuple with empty string values.
    """
    # Return a tuple with empty string values to suppress test status output
    # but allow our own custom output to show
    return "", "", ""


# Direct approach to disable the summary
@pytest.hookimpl(trylast=True)
def pytest_sessionstart(session):
    """Session startup hook - directly modify the session object early"""
    global session_start_time
    # Start timing the session
    session_start_time = time.time()

    # Log information about detailed request log files
    try:
        import os
        from importlib import import_module
        
        # Import the RequestDebugger
        request_debugger_module = import_module('e2e_tests.rosetta_client.request_debugger')
        log_dir = os.path.join(os.getcwd(), "logs")
        
        # Check if log directory exists and has log files
        if os.path.exists(log_dir):
            log_files = [f for f in os.listdir(log_dir) if f.startswith('requests_') and f.endswith('.log')]
            if log_files:
                latest_log = sorted(log_files)[-1]  # Get the latest log file
                log_path = os.path.join(log_dir, latest_log)
                logging.getLogger("test").info(f"Detailed API request logs will be saved to: {log_path}")
    except Exception as e:
        logging.getLogger("test").debug(f"Could not determine log file path: {str(e)}")

    # Get the terminal reporter directly from the session
    if hasattr(session, "config") and hasattr(session.config, "pluginmanager"):
        terminal = session.config.pluginmanager.getplugin("terminalreporter")
        if terminal:
            # Create a do-nothing summary function
            def disabled_summary(*args, **kwargs):
                return None

            # Replace reporter's methods
            terminal.summary_stats = disabled_summary

            # Try to find any other summary methods
            for attr_name in dir(terminal):
                if attr_name.startswith("summary_") and callable(
                    getattr(terminal, attr_name)
                ):
                    setattr(terminal, attr_name, disabled_summary)


# Add a hook to store all collected tests
@pytest.hookimpl(trylast=True)
def pytest_collection_modifyitems(session, config, items):
    """
    This hook runs after test collection.
    We'll store all collected tests here.
    """
    # Store the test items for later use
    collected_tests["items"] = items
    collected_tests["nodeids"] = [item.nodeid for item in items]


# Use a direct approach to block terminal reporter's summary generation
@pytest.hookimpl(hookwrapper=True)
def pytest_unconfigure(config):
    """
    Final hook that runs at the very end of pytest execution.
    Use this to print our summary as the very last thing and BLOCK the default summary.
    """
    # Get the terminal reporter
    terminal = config.pluginmanager.getplugin("terminalreporter")
    if terminal:
        # Clear the terminal reporter's stats to prevent it from generating a summary
        # Check if stats exists before clearing
        if hasattr(terminal, "stats") and terminal.stats:
             terminal.stats.clear()

        # Create empty methods for summary reporting
        terminal.summary_stats = lambda: None
        terminal.summary_errors = lambda: None
        terminal.summary_failures = lambda: None
        terminal.summary_warnings = lambda: None

        # Let other hooks run
        yield

        # Calculate session duration from our own timer
        duration = 0
        if session_start_time is not None:
            duration = time.time() - session_start_time
        # Fall back to pytest's duration if available
        elif hasattr(config, "_session") and hasattr(config._session, "duration"):
            duration = config._session.duration

        # Print the separator line first
        print("\n" + "=" * 80)

        # Generate the summary
        if completed_tests:
            # Count the outcomes correctly
            passed = sum(1 for info in completed_tests.values() if info["outcome"] == "passed")
            failed = sum(1 for info in completed_tests.values() if info["outcome"] == "failed")
            skipped = sum(1 for info in completed_tests.values() if info["outcome"] == "skipped")
            total_completed = len(completed_tests)
            total_collected = len(collected_tests["nodeids"])

            # Print table header with clean separator
            print(f"{Style.GRAY}TEST RESULTS{Style.RESET}")
            print(f"{Style.GRAY}{'─' * 80}{Style.RESET}")

            # Calculate column widths
            status_width = 8
            duration_width = 10
            test_width = 60  # Maximum width for test name

            # Print header row
            print(
                f"{Style.BOLD}{'STATUS':<{status_width}} {'DURATION':<{duration_width}} {'TEST':<{test_width}}{Style.RESET}"
            )
            print(f"{Style.GRAY}{'─' * 80}{Style.RESET}")

            # Sort completed tests by nodeid for consistent display
            sorted_tests = sorted(completed_tests.items(), key=lambda x: x[0])

            # Print each test result
            for nodeid, result in sorted_tests:
                # Format test name: remove common prefix and make more readable
                test_name = nodeid
                if "::" in test_name:
                    # Extract just the test function part and any parameters
                    test_name = test_name.split("::")[-1]

                # Trim if too long
                if len(test_name) > test_width:
                    test_name = test_name[: test_width - 3] + "..."

                # Format status with appropriate color
                if result["outcome"] == "passed":
                    status = f"{Style.GREEN}{'✓':<{status_width-1}}{Style.RESET}"
                elif result["outcome"] == "failed":
                    status = f"{Style.RED}{'✗':<{status_width-1}}{Style.RESET}"
                elif result["outcome"] == "skipped":
                    status = f"{Style.YELLOW}{'○':<{status_width-1}}{Style.RESET}"
                else:
                    status = f"{Style.GRAY}{'?':<{status_width-1}}{Style.RESET}"

                # Format duration
                duration_str = f"{result['duration']:.2f}s"

                # Print formatted row
                print(f"{status} {duration_str:<{duration_width}} {test_name}")

            print(f"{Style.GRAY}{'─' * 80}{Style.RESET}")

            # Print our custom summary after the test results table
            print(
                f"{Style.GRAY}SUMMARY · {Style.RESET}{Style.BOLD}{duration:.2f}s{Style.RESET} · "
                f"{Style.GREEN}✓{Style.RESET} {passed} passed · "
                f"{Style.RED}✗{Style.RESET} {failed} failed · "
                f"{Style.YELLOW}○{Style.RESET} {skipped} skipped · "
                f"{Style.BLUE}{total_completed}/{total_collected}{Style.RESET} total"
            )
            print("=" * 80)
        else:
            # No tests were executed, show basic summary
            print(f"{Style.GRAY}No tests executed. {Style.RESET}")
            print("=" * 80)

        # Restore stdout if redirected
        if hasattr(sys, "_original_stdout"):
            sys.stdout = sys._original_stdout
            delattr(sys, "_original_stdout")
        if hasattr(sys, "_log_file"):
            sys._log_file.close()
            delattr(sys, "_log_file")

    else:
        # No terminal reporter found, just let other hooks run
        yield
        # Clean up redirection if it happened
        if hasattr(sys, "_original_stdout"):
            sys.stdout = sys._original_stdout
            delattr(sys, "_original_stdout")
        if hasattr(sys, "_log_file"):
            sys._log_file.close()
            delattr(sys, "_log_file")


# Add a simple hook for test section clarity
@pytest.hookimpl(trylast=True)
def pytest_runtest_setup(item):
    """Add a clear separator before each test."""
    # Always print a clear separator and the full test name
    # Don't print if running with -q (quiet)
    if item.config.option.verbose >= 0: # Only print if not quiet (-q)
        print(f"\n{Style.GRAY}{'─' * 80}{Style.RESET}")
        print(f"{Style.BOLD}{item.nodeid}{Style.RESET}\n")


# Handle test result reporting with our custom styling
@pytest.hookimpl(trylast=True)
def pytest_runtest_logreport(report):
    """Display result at the end of each test with minimalist design principles."""
    # We only care about the setup phase (for skips) and call phase (for pass/fail)
    # Teardown phase is ignored for test result purposes
    if report.when == "teardown":
        return

    nodeid = report.nodeid
    
    # --- Handle Setup Failures/Skips --- 
    if report.when == "setup":
        if report.failed:
            if nodeid not in reported_tests:
                reported_tests.add(nodeid)
                # Print failure details immediately
                print(f"\n{Style.RED}× {Style.BOLD}SETUP FAILED{Style.RESET}")
                _print_failure_details(report)
                completed_tests[nodeid] = {
                    "outcome": "failed",
                    "duration": getattr(report, "duration", 0.0),
                    "phase": "setup"
                }
            return # Stop processing for this test
        
        elif report.skipped:
            if nodeid not in reported_tests:
                reported_tests.add(nodeid)
                completed_tests[nodeid] = {
                    "outcome": "skipped",
                    "duration": getattr(report, "duration", 0.0),
                    "phase": "setup"
                }
                # Extract skip reason
                reason = ""
                if hasattr(report, "longrepr"):
                    if isinstance(report.longrepr, tuple) and len(report.longrepr) >= 3:
                        reason = report.longrepr[2]
                    elif isinstance(report.longrepr, str):
                        reason = report.longrepr
                    if "Skipped:" in reason:
                        reason = reason.split("Skipped:")[1].strip().strip("'")
                print(f"\n{Style.YELLOW}○ {Style.BOLD}SKIPPED{Style.RESET} {Style.GRAY}{reason}{Style.RESET}")
            return # Stop processing for this test
        
        # If setup passed, do nothing yet, wait for call phase
        return
        
    # --- Handle Call Phase --- 
    if report.when == "call":
        if nodeid not in reported_tests: # Only report the first outcome (call takes precedence over setup pass)
            reported_tests.add(nodeid)
            completed_tests[nodeid] = {
                "outcome": report.outcome,
                "duration": getattr(report, "duration", 0.0),
                "phase": "call"
            }
            
            if report.passed:
                print(f"\n{Style.GREEN}✓ {Style.BOLD}PASSED{Style.RESET}")
                _print_request_debugger_summary(report)
            elif report.failed:
                # Print failure details immediately
                print(f"\n{Style.RED}× {Style.BOLD}FAILED{Style.RESET}")
                _print_failure_details(report)
                _print_request_debugger_summary(report)
            # No explicit message for skipped during call phase (handled in setup)

# Helper function to print failure details
def _print_failure_details(report):
    if hasattr(report, "longrepr") and report.longrepr:
        print(f"\n{Style.RED}══════ ERROR DETAILS ══════{Style.RESET}")
        if isinstance(report.longrepr, tuple) and len(report.longrepr) >= 3:
            # Standard traceback tuple (file, lineno, message)
            _, lineno, error_msg = report.longrepr
            print(f"{Style.RED}Error source: {report.longrepr[0]} at line {lineno}{Style.RESET}")
            lines = str(error_msg).strip().split("\n")
        elif hasattr(report.longrepr, 'reprcrash'):
             # Exception info from reprcrash (more detailed)
             crashinfo = report.longrepr.reprcrash
             print(f"{Style.RED}Error source: {crashinfo.path} at line {crashinfo.lineno}{Style.RESET}")
             lines = crashinfo.message.strip().split("\n")
        elif isinstance(report.longrepr, str):
            # Simple string representation
            lines = report.longrepr.strip().split("\n")
        else:
            # Fallback
            lines = ["Could not extract detailed error info."]

        # Print more lines of the error message (up to 25)
        for line in lines[:25]:
            if line.strip().startswith("E   ") or line.strip().startswith("AssertionError:"):
                print(f"{Style.RED}{line}{Style.RESET}")
            else:
                print(f"{Style.GRAY}  {line}{Style.RESET}")
        
        if len(lines) > 25:
            print(f"{Style.GRAY}  ... ({len(lines) - 25} more lines){Style.RESET}")
        
        print(f"{Style.RED}═════════════════════════{Style.RESET}\n")

# Helper function to print request debugger summary (if DEBUG enabled)
def _print_request_debugger_summary(report):
     if logger.isEnabledFor(logging.DEBUG) and hasattr(report, "node") and hasattr(report.node, "funcargs"):
        if "rosetta_client" in report.node.funcargs:
            client = report.node.funcargs["rosetta_client"]
            if hasattr(client, "request_debugger"):
                # Use logger.debug for this summary, so it only shows with --log-cli-level=DEBUG
                client.request_debugger.print_summary_report()


def pytest_configure(config):
    """Configure basic pytest options."""
    # Detect if --log-cli-level was specified
    if config.option.log_cli_level:
        # Set log_cli_level from command line
        config.option.log_cli = True
        
        # Override pytest's default format
        config.option.log_cli_format = "%(asctime)s %(levelname)s %(message)s"
        config.option.log_cli_date_format = "%H:%M:%S"
    else:
        # Set log_cli_level from environment variable
        log_cli_level = os.getenv("LOG_CLI_LEVEL", "INFO")
        config.option.log_cli = log_cli_level.upper() == "DEBUG"
        config.option.log_cli_format = "%(asctime)s %(levelname)s %(message)s"
        config.option.log_cli_date_format = "%H:%M:%S"

    # These options are now mostly handled by pytest.ini
    # config.option.verbose = 0 # Controlled by addopts
    # config.option.no_summary = True # Controlled by addopts
    # config.option.no_header = True # Controlled by addopts
    pass # Keep this hook minimal, rely on pytest.ini


# Force disable pytest's built-in summary with a more direct approach
# Note: This might be fragile depending on pytest version
if hasattr(_pytest.terminal, "TerminalReporter"):
    _pytest.terminal.TerminalReporter.summary_stats = lambda self: None


# Add a hook that runs when pytest finds plugins to ensure our override is effective
@pytest.hookimpl(trylast=True)
def pytest_plugin_registered(plugin, manager):
    """
    This hook is called after a plugin is registered.
    We'll use it to ensure our override of the summary methods is maintained.
    """
    if isinstance(plugin, _pytest.terminal.TerminalReporter):
        # Ensure the plugin's summary methods are all disabled
        plugin.summary_stats = lambda: None
        plugin.summary_failures = lambda: None
        plugin.summary_warnings = lambda: None
        plugin.summary_deselected = lambda: None
        if hasattr(plugin, "print_summary"):
            plugin.print_summary = lambda: None
