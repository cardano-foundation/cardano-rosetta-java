import os
from dotenv import load_dotenv
import pytest
import logging
import time
import _pytest.terminal
import sys

from rosetta_client.client import RosettaClient
from wallet_utils.pycardano_wallet import PyCardanoWallet

# Load environment variables from .env file
load_dotenv()


# ANSI color/style constants with minimalist design principles
class Style:
    # Colors - using a more restrained palette
    BLUE = "\033[38;5;68m"  # Softer blue
    GREEN = "\033[38;5;71m"  # Muted green
    YELLOW = "\033[38;5;179m"  # Softer yellow
    RED = "\033[38;5;167m"  # Muted red
    GRAY = "\033[38;5;246m"  # Medium gray
    CYAN = "\033[38;5;109m"  # Muted cyan

    # Styles
    BOLD = "\033[1m"
    RESET = "\033[0m"

    # Icons - minimal and functional
    DEBUG_ICON = "•"
    INFO_ICON = "→"
    WARNING_ICON = "!"
    ERROR_ICON = "×"
    CRITICAL_ICON = "‼"
    HTTP_ICON = "⤷"


# Logging level configuration with colors and icons
LOG_LEVELS = {
    "DEBUG": (Style.GRAY, Style.DEBUG_ICON),
    "INFO": (Style.BLUE, Style.INFO_ICON),
    "WARNING": (Style.YELLOW, Style.WARNING_ICON),
    "ERROR": (Style.RED, Style.ERROR_ICON),
    "CRITICAL": (Style.RED + Style.BOLD, Style.CRITICAL_ICON),
}


class SwissDesignFormatter(logging.Formatter):
    """Custom log formatter with minimalist design principles"""

    def formatTime(self, record, datefmt=None):
        """Format timestamp with gray styling"""
        asctime = super().formatTime(record, datefmt)
        return f"{Style.GRAY}{asctime}{Style.RESET}"

    def format(self, record):
        # Simplify logger name
        original_name = record.name
        if "." in original_name:
            record.name = original_name.split(".")[0]

        # Format level name with appropriate design principles
        if record.levelname in LOG_LEVELS:
            color, icon = LOG_LEVELS[record.levelname]
            # Right-aligned level indicators with consistent width
            record.levelname = f"{color}{icon}{Style.RESET}"

        # Format names with consistent width for grid alignment
        record.name = f"{Style.GRAY}{record.name:<10}{Style.RESET}"

        # Create a minimal format for INFO level logs - just the essential message
        if record.levelno == logging.INFO:
            # Format HTTP-related logs in a minimal way
            if hasattr(record, "msg") and isinstance(record.msg, str):
                msg = record.msg

                # Special formatting for test logs
                if original_name == "test":
                    if "Starting" in msg and "transaction test" in msg:
                        # Make the "Starting test" message stand out with green color
                        # Apply singular/plural consistency
                        msg = msg.replace(" with ", " · ")
                        msg = msg.replace("1 inputs", "1 input")
                        msg = msg.replace("1 outputs", "1 output")
                        record.msg = f"{Style.GREEN}{msg}{Style.RESET}"
                    elif "Transaction submitted successfully" in msg:
                        # Highlight transaction IDs in a different color
                        parts = msg.split(" - ID: ")
                        if len(parts) == 2:
                            tx_id_part = parts[1].split(" ")[0]
                            remaining = parts[1][len(tx_id_part) :]
                            # Add success icon and replace dash with dot separator
                            record.msg = f"{Style.GREEN}✓ Transaction submitted successfully{Style.RESET} · ID: {Style.CYAN}{tx_id_part}{Style.RESET}{Style.GRAY}{remaining}{Style.RESET}"
                    elif "Transaction found in block" in msg:
                        # Highlight block info
                        parts = msg.split("Transaction found in block ")
                        if len(parts) == 2:
                            # Add success icon
                            record.msg = f"{Style.GREEN}✓ Transaction found in block {Style.CYAN}{parts[1]}{Style.RESET}"
                    elif "Validating" in msg or "Waiting" in msg:
                        # Light gray for processing messages
                        record.msg = f"{Style.GRAY}{msg}{Style.RESET}"
                    elif "successfully validated" in msg:
                        # Bold green for success messages with success icon
                        # Replace parentheses with minimal dot separators for better Swiss design
                        msg = msg.replace(" (", " · ")
                        msg = msg.replace(", ", " · ")
                        msg = msg.replace(")", "")
                        record.msg = f"{Style.GREEN}{Style.BOLD}✓ {msg}{Style.RESET}"

                # Highly minimal HTTP request/response formatting
                elif "[REQUEST " in msg:
                    # Extract method and endpoint
                    parts = msg.split(" ", 2)
                    method = parts[1] if len(parts) > 1 else ""

                    # Extract the full endpoint path
                    endpoint = ""
                    if len(parts) > 2 and "http" in parts[2]:
                        url_parts = parts[2].split(" ")[0]  # Get the URL part
                        # Extract path from URL (e.g., /construction/submit from http://localhost:8082/construction/submit)
                        if "/" in url_parts:
                            path_parts = url_parts.split("/")
                            # Reconstruct the path starting from the 3rd part (after http://domain/)
                            if len(path_parts) > 3:
                                endpoint = "/" + "/".join(path_parts[3:])

                    if endpoint:
                        record.msg = f"{Style.CYAN}{method}{Style.RESET} {Style.GRAY}{endpoint}{Style.RESET}"
                    else:
                        record.msg = f"{Style.CYAN}{method}{Style.RESET}"
                        
                    # For HTTP requests/responses, use a custom format without file/line number
                    return f"{self.formatTime(record, self.datefmt)}  {record.levelname}  {Style.GRAY}http{' '*7}{Style.RESET}  {record.getMessage()}"
                    
                elif "[RESPONSE " in msg:
                    if "Status: 2" in msg or "Status: 3" in msg:
                        # Success response - just show status code and timing
                        status_code = (
                            msg.split("Status:")[1].strip().split()[0]
                            if "Status:" in msg
                            else ""
                        )
                        timing = (
                            msg.split("(")[1].split(")")[0]
                            if "(" in msg and ")" in msg
                            else ""
                        )
                        record.msg = (
                            f"{Style.GREEN}✓{Style.RESET} {status_code} · {timing}"
                        )
                    else:
                        # Error response - show status code clearly
                        status_code = (
                            msg.split("Status:")[1].strip().split()[0]
                            if "Status:" in msg
                            else ""
                        )
                        # Try to extract endpoint information for more context
                        endpoint = ""
                        if "Endpoint:" in msg:
                            endpoint_parts = msg.split("Endpoint:")
                            if len(endpoint_parts) > 1:
                                endpoint = endpoint_parts[1].strip()

                        if endpoint:
                            record.msg = (
                                f"{Style.RED}✗{Style.RESET} {status_code} · {endpoint}"
                            )
                        else:
                            record.msg = f"{Style.RED}✗{Style.RESET} {status_code}"
                            
                    # For HTTP requests/responses, use a custom format without file/line number
                    return f"{self.formatTime(record, self.datefmt)}  {record.levelname}  {Style.GRAY}http{' '*7}{Style.RESET}  {record.getMessage()}"
                elif "[ERROR " in msg:
                    # Extract just the core error message
                    error_msg = msg.split(" ", 1)[1] if " " in msg else msg
                    # Make error messages more concise by extracting key information
                    if " - " in error_msg:
                        parts = error_msg.split(" - ")
                        error_type = parts[0].strip()
                        error_detail = parts[1].strip() if len(parts) > 1 else ""
                        record.msg = (
                            f"{Style.RED}✗{Style.RESET} {error_type} · {error_detail}"
                        )
                    else:
                        record.msg = f"{Style.RED}✗{Style.RESET} {error_msg}"

                # Test logs - keep them informative but concise
                elif original_name.strip() == "test":
                    # Keep test logs informative but minimal
                    record.msg = f"{Style.BLUE}{msg}{Style.RESET}"

            # Use a minimal format for INFO logs
            return f"{self.formatTime(record, self.datefmt)}  {record.levelname}  {record.name}  {record.getMessage()}"

        # For non-INFO logs (DEBUG, WARNING, ERROR, CRITICAL), use the detailed format
        else:
            # Special formatting for HTTP-related logs with detailed data at DEBUG level
            if hasattr(record, "msg") and isinstance(record.msg, str):
                msg = record.msg

                # Format HTTP request logs with full payload data
                if "[REQUEST " in msg:
                    # Extract and include request data
                    record.msg = f"{Style.CYAN}{Style.HTTP_ICON} {Style.RESET} {msg}"
                    
                    # Add payload data if available
                    if "Payload:" in msg:
                        data_parts = msg.split("Payload:", 1)
                        if len(data_parts) > 1:
                            payload = data_parts[1].strip()
                            # Add the payload data in a formatted block
                            record.msg = f"{Style.CYAN}{Style.HTTP_ICON} REQUEST DETAILS {Style.RESET}\n{data_parts[0]}\n{Style.GRAY}Payload: {payload}{Style.RESET}"
                
                # Format HTTP response logs with full response data
                elif "[RESPONSE " in msg:
                    response_style = Style.GREEN if "Status: 2" in msg or "Status: 3" in msg else Style.RED
                    
                    # Add response data if available
                    if "Body:" in msg:
                        data_parts = msg.split("Body:", 1)
                        if len(data_parts) > 1:
                            body = data_parts[1].strip()
                            # Add the response body in a formatted block
                            record.msg = f"{response_style}{Style.HTTP_ICON} RESPONSE DETAILS {Style.RESET}\n{data_parts[0]}\n{Style.GRAY}Body: {body}{Style.RESET}"
                    else:
                        record.msg = f"{response_style}{Style.HTTP_ICON} {Style.RESET} {msg}"
                
                elif "[ERROR " in msg:
                    record.msg = f"{Style.RED}{Style.HTTP_ICON} {Style.RESET} {msg}"

            # Emphasizes precision and breathing space
            formatted = super().format(record)

            # Grid-like structure for errors and warnings
            if record.levelno >= logging.WARNING:
                # Add structured spacing before warnings/errors - precisely 80 characters
                separator = f"\n{Style.GRAY}{'─' * 80}{Style.RESET}\n"
                formatted = f"{separator}{formatted}"

                # Add clear spacing after for visual breathing room
                formatted = f"{formatted}\n"

            return formatted


# ------------- PYTEST HOOKS -------------
# These hooks implement a custom reporting system that:
# 1. Suppresses pytest's built-in markers (F, s, ., etc.)
# 2. Disables the built-in summary output
# 3. Provides a clean, accurate summary at the end of test execution
# 4. Uses minimalist design principles for clarity and consistency


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


# Store collected tests for accurate reporting
collected_tests = {"items": [], "nodeids": []}
# Track completed test results by nodeid
completed_tests = {}
# Track which tests have already been reported in output
reported_tests = set()
# Track session start time for accurate duration reporting
session_start_time = None


# Direct approach to disable the summary
@pytest.hookimpl(trylast=True)
def pytest_sessionstart(session):
    """Session startup hook - directly modify the session object early"""
    global session_start_time
    # Start timing the session
    session_start_time = time.time()

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

        # Now restore stdout and close the log file
        if hasattr(sys, "_original_stdout") and hasattr(sys, "_log_file"):
            sys.stdout = sys._original_stdout
            sys._log_file.close()
            delattr(sys, "_original_stdout")
            delattr(sys, "_log_file")
    else:
        # No terminal reporter found, just let other hooks run
        yield

        # Still clean up stdout redirection
        if hasattr(sys, "_original_stdout") and hasattr(sys, "_log_file"):
            sys.stdout = sys._original_stdout
            sys._log_file.close()
            delattr(sys, "_original_stdout")
            delattr(sys, "_log_file")


# Add a simple hook for test section clarity
@pytest.hookimpl(trylast=True)
def pytest_runtest_setup(item):
    """Add a clear separator before each test."""
    # Always print a clear separator and the full test name
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
    
    # For setup failures, we want to report those as test failures
    if report.when == "setup" and report.failed:
        if nodeid not in reported_tests:
            reported_tests.add(nodeid)
            print(f"\n{Style.RED}× {Style.BOLD}FAILED (setup){Style.RESET}")
            completed_tests[nodeid] = {
                "outcome": "failed",
                "duration": getattr(report, "duration", 0.0),
                "phase": "setup"
            }
            # Show error details for setup failures
            if hasattr(report, "longrepr") and report.longrepr:
                if isinstance(report.longrepr, tuple) and len(report.longrepr) >= 3:
                    _, _, error_msg = report.longrepr
                    lines = str(error_msg).strip().split("\n")
                    print(f"{Style.RED}Error details:{Style.RESET}")
                    for line in lines[:10]:
                        print(f"{Style.GRAY}  {line.strip()}{Style.RESET}")
                    if len(lines) > 10:
                        print(f"{Style.GRAY}  ... ({len(lines) - 10} more lines){Style.RESET}")
                elif isinstance(report.longrepr, str):
                    print(f"{Style.RED}Error: {Style.GRAY}{report.longrepr}{Style.RESET}")
        return
    
    # For skipped tests, only report once during setup phase
    if report.when == "setup" and report.skipped:
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
        return
        
    # For call phase, this is the "real" test result
    if report.when == "call":
        if nodeid not in reported_tests:
            reported_tests.add(nodeid)
            completed_tests[nodeid] = {
                "outcome": report.outcome,
                "duration": getattr(report, "duration", 0.0),
                "phase": "call"
            }
            
            if report.passed:
                print(f"\n{Style.GREEN}✓ {Style.BOLD}PASSED{Style.RESET}")
                # Add request metrics if available
                if hasattr(report, "node") and hasattr(report.node, "funcargs"):
                    if "rosetta_client" in report.node.funcargs:
                        client = report.node.funcargs["rosetta_client"]
                        if hasattr(client, "request_debugger"):
                            client.request_debugger.print_summary_report()
            elif report.failed:
                print(f"\n{Style.RED}× {Style.BOLD}FAILED{Style.RESET}")
                # Add request metrics if available
                if hasattr(report, "node") and hasattr(report.node, "funcargs"):
                    if "rosetta_client" in report.node.funcargs:
                        client = report.node.funcargs["rosetta_client"]
                        if hasattr(client, "request_debugger"):
                            client.request_debugger.print_summary_report()
                # Add detailed failure information
                if hasattr(report, "longrepr") and report.longrepr:
                    if isinstance(report.longrepr, tuple) and len(report.longrepr) >= 3:
                        _, _, error_msg = report.longrepr
                        lines = str(error_msg).strip().split("\n")
                        print(f"{Style.RED}Error details:{Style.RESET}")
                        for line in lines[:10]:
                            print(f"{Style.GRAY}  {line.strip()}{Style.RESET}")
                        if len(lines) > 10:
                            print(f"{Style.GRAY}  ... ({len(lines) - 10} more lines){Style.RESET}")
                    elif isinstance(report.longrepr, str):
                        print(f"{Style.RED}Error: {Style.GRAY}{report.longrepr}{Style.RESET}")


def pytest_configure(config):
    """Configure logging for tests with minimalist design principles"""
    # Basic settings to disable built-in reports
    config.option.verbose = 0
    config.option.no_summary = True
    config.option.no_header = True
    config.option.no_progressbar = True

    # Set log_cli_level from command line or environment variable
    if not config.option.log_cli_level:
        config.option.log_cli = False
        config.option.log_cli_level = os.environ.get('LOG_LEVEL', 'INFO')
    else:
        # CLI logging is being used, customize its format
        config.option.log_cli = True
        # Use a minimal format for CLI logging to hide file locations
        config.option.log_cli_format = "%(asctime)s %(levelname)s %(message)s"
        config.option.log_cli_date_format = "%H:%M:%S"

    # Simplify terminal reporter handling
    terminal = config.pluginmanager.getplugin("terminalreporter")
    if terminal:
        # Disable the path info in reports
        terminal.showfspath = False

        # Override the test status reporting method
        def custom_write_fspath_result(nodeid, res, **kwargs):
            # Suppress the output
            pass

        terminal.write_fspath_result = custom_write_fspath_result.__get__(terminal)

    # Configure root logger with minimalist formatter
    handler = logging.StreamHandler()
    formatter = SwissDesignFormatter(
        fmt="%(asctime)s  %(levelname)s  %(name)s  %(message)s",
        datefmt="%H:%M:%S",
    )
    handler.setFormatter(formatter)
    
    # Set handler level based on command line or environment
    log_level = config.option.log_cli_level
    handler.setLevel(getattr(logging, log_level) if hasattr(logging, log_level) else logging.INFO)

    # Add a file handler with a simple formatter (no ANSI colors)
    file_handler = logging.FileHandler("test_output.log", mode="w")
    file_formatter = logging.Formatter(
        fmt="%(asctime)s  %(levelname)s  %(name)s  %(message)s",
        datefmt="%H:%M:%S",
    )
    file_handler.setFormatter(file_formatter)
    file_handler.setLevel(logging.DEBUG)  # Always keep debug level for the file

    root_logger = logging.getLogger()
    root_logger.handlers = [handler, file_handler]  # Add file_handler here
    root_logger.setLevel(logging.DEBUG)  # Set to DEBUG to capture all logs

    # Silence noisy libraries
    logging.getLogger("urllib3").setLevel(logging.ERROR)
    logging.getLogger("pycardano").setLevel(logging.WARNING)
    logging.getLogger("requests").setLevel(logging.ERROR)
    logging.getLogger("asyncio").setLevel(logging.WARNING)
    logging.getLogger("pytest").setLevel(logging.ERROR)

    # Configure rosetta client logging
    client_logger = logging.getLogger("rosetta_client")
    client_logger.setLevel(logging.DEBUG)  # Set to DEBUG to capture detailed request/response data

    # Configure HTTP request logging specifically
    http_logger = logging.getLogger("rosetta_client.http")
    http_logger.setLevel(logging.DEBUG)  # Set to DEBUG to capture detailed request/response data

    # Silence wallet_utils messages at INFO level (only show in DEBUG)
    wallet_logger = logging.getLogger("wallet_utils")
    wallet_logger.setLevel(logging.WARNING)  # Only show warnings from wallet_utils

    # Create a logger for our tests with a more concise name
    test_logger = logging.getLogger("test")
    test_logger.setLevel(logging.INFO)  # Show minimal INFO logs by default


@pytest.fixture(scope="session")
def rosetta_client():
    endpoint = os.environ.get("ROSETTA_ENDPOINT", "https://testnet.rosetta-api.io")
    network = os.environ.get("CARDANO_NETWORK", "testnet")
    return RosettaClient(endpoint=endpoint, network=network)


@pytest.fixture(scope="session")
def test_wallet():
    mnemonic = os.environ.get("TEST_WALLET_MNEMONIC", "palavras de teste...")
    return PyCardanoWallet.from_mnemonic(mnemonic, network="testnet")


# Force disable pytest's built-in summary with a more direct approach
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


@pytest.fixture(scope="session", autouse=True)
def redirect_stdout_to_file():
    class Tee:
        def __init__(self, original_stdout, file):
            self.original_stdout = original_stdout
            self.file = file

        def write(self, message):
            self.original_stdout.write(message)
            self.file.write(self.strip_ansi(message))

        def flush(self):
            self.original_stdout.flush()
            self.file.flush()

        @staticmethod
        def strip_ansi(text):
            import re

            ansi_escape = re.compile(r"\x1B\[[0-?]*[ -/]*[@-~]")
            return ansi_escape.sub("", text)

    original_stdout = sys.stdout
    log_file = open("test_output.log", "a")
    sys.stdout = Tee(original_stdout, log_file)
    yield
    # Don't restore stdout immediately - let it capture the summary
    # We'll restore it in pytest_unconfigure after the summary is printed

    # Store these for cleanup in pytest_unconfigure
    sys._original_stdout = original_stdout
    sys._log_file = log_file
