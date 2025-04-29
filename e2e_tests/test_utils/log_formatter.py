import logging
import re
import time

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

        # Get the original message
        msg = record.getMessage()

        # For INFO level logs - apply custom formatting
        if record.levelno == logging.INFO:
            # Special formatting for HTTP logger
            if original_name.strip() == "http":
                # Color HTTP status indicators if not already colored
                if "✓" in msg and Style.GREEN not in msg:
                    msg = msg.replace("✓", f"{Style.GREEN}✓{Style.RESET}")
                elif "✗" in msg and Style.RED not in msg:
                    msg = msg.replace("✗", f"{Style.RED}✗{Style.RESET}")
                record.msg = msg
            else:
                # Generic formatting based on message structure
                
                # Check for patterns in order of specificity
                
                # 1. Messages starting with checkmark (priority over other patterns)
                if msg.startswith("✓ "):
                    # Format messages that start with checkmark but don't end with !
                    parts = msg[2:].split(" · ")  # Remove checkmark and split
                    formatted_parts = [f"{Style.GREEN}✓{Style.RESET} {Style.BLUE}{parts[0]}{Style.RESET}"]
                    
                    # Format remaining parts
                    if len(parts) > 1:
                        for part in parts[1:]:
                            if ":" in part:
                                key, value = part.split(":", 1)
                                formatted_parts.append(f"{Style.GRAY}{key.strip()}:{Style.RESET} {Style.CYAN}{value.strip()}{Style.RESET}")
                            else:
                                formatted_parts.append(part)
                    
                    record.msg = " · ".join(formatted_parts)
                
                # 2. Success messages (ending with !)
                elif msg.endswith("!"):
                    base_msg = msg[:-1].strip()
                    parts = base_msg.split(" · ")
                    
                    # First part gets green checkmark for success
                    formatted_parts = [f"{Style.GREEN}✓ {parts[0]}{Style.RESET}"]
                    
                    # Format remaining parts
                    if len(parts) > 1:
                        for part in parts[1:]:
                            if ":" in part:
                                key, value = part.split(":", 1)
                                formatted_parts.append(f"{Style.GRAY}{key.strip()}:{Style.RESET} {Style.CYAN}{value.strip()}{Style.RESET}")
                            else:
                                formatted_parts.append(part)
                    
                    record.msg = " · ".join(formatted_parts)
                
                # 3. Messages with colon but no dots (e.g., "Using Endpoint: value")
                elif ":" in msg and " · " not in msg:
                    # Simple key-value message
                    if msg.count(":") == 1:  # Only one colon
                        key, value = msg.split(":", 1)
                        record.msg = f"{Style.BLUE}{key.strip()}:{Style.RESET} {Style.CYAN}{value.strip()}{Style.RESET}"
                    else:
                        # Multiple colons but no dots - make the first part blue
                        record.msg = f"{Style.BLUE}{msg}{Style.RESET}"
                
                # 4. Messages with key-value pairs using dots
                elif " · " in msg and ":" in msg:
                    parts = msg.split(" · ")
                    formatted_parts = []
                    
                    # Format each part
                    for i, part in enumerate(parts):
                        if ":" in part:
                            # Format key-value pairs
                            key, value = part.split(":", 1)
                            formatted_parts.append(f"{Style.GRAY}{key.strip()}:{Style.RESET} {Style.CYAN}{value.strip()}{Style.RESET}")
                        else:
                            # First part in blue, others as is
                            if i == 0:
                                formatted_parts.append(f"{Style.BLUE}{part}{Style.RESET}")
                            else:
                                formatted_parts.append(part)
                    
                    record.msg = " · ".join(formatted_parts)
                
                # 5. All other INFO messages in blue
                else:
                    record.msg = f"{Style.BLUE}{msg}{Style.RESET}"

            # Return minimal format for INFO logs
            return f"{self.formatTime(record, self.datefmt)}  {record.levelname}  {record.name}  {record.getMessage()}"

        # For non-INFO logs (DEBUG, WARNING, ERROR, CRITICAL), use detailed format
        else:
            # Use standard formatting
            formatted = super().format(record)

            # Grid-like structure for errors and warnings
            if record.levelno >= logging.WARNING:
                # Add structured separator for warnings/errors
                separator = f"\n{Style.GRAY}{'─' * 80}{Style.RESET}\n"
                formatted = f"{separator}{formatted}"

                # Add breathing room after errors/warnings
                formatted = f"{formatted}\n"

            return formatted 