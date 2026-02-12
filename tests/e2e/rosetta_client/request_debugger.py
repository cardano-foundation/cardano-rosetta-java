import time
import logging
import json
import requests
import urllib.parse
import uuid
import os
import datetime
from typing import Dict, List, Optional, Any, Tuple

# Define colors directly here instead of importing Style to avoid circular imports
GREEN = "\033[38;5;46m"   # Bright green
RED = "\033[38;5;196m"    # Bright red
CYAN = "\033[38;5;109m"  # Muted cyan
GRAY = "\033[38;5;246m"   # Medium gray
RESET = "\033[0m"
YELLOW = "\033[38;5;226m"  # Muted yellow

# Create a dedicated logger for HTTP requests that matches the original format
http_logger = logging.getLogger("rosetta_client.http")

# Create a file logger for detailed request/response logging
detailed_logger = logging.getLogger("rosetta_client.detailed")
detailed_logger.setLevel(logging.DEBUG)
detailed_logger.propagate = False  # Don't send to root logger

# Create log directory if it doesn't exist
log_dir = os.path.join(os.getcwd(), "logs")
os.makedirs(log_dir, exist_ok=True)

# Add a file handler for detailed logging
log_filename = os.path.join(log_dir, f"requests_{datetime.datetime.now().strftime('%Y%m%d_%H%M%S')}.log")
file_handler = logging.FileHandler(log_filename, mode='w')
file_handler.setLevel(logging.DEBUG)
file_formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
file_handler.setFormatter(file_formatter)
detailed_logger.addHandler(file_handler)

class RequestDebugger:
    """
    Handles request debugging and metrics tracking for API calls.
    """
    def __init__(self):
        self.requests = []
        self.request_times = {}
        self.start_time = time.time()
        self.pending_requests = {}  # Store request info while waiting for response
        self.log_file_path = log_filename
        detailed_logger.info(f"Request logging started. Log file: {self.log_file_path}")

    def post(self, url: str, **kwargs) -> requests.Response:
        """
        Send a POST request and log detailed metrics and potential errors.
        
        Args:
            url: The URL to send the request to
            **kwargs: Additional arguments to pass to requests.post()
            
        Returns:
            Response object from the request
            
        Raises:
            Any exceptions raised by the underlying requests library
        """
        # Record start time
        start_time = time.time()
        
        # Parse URL for logging - use the full endpoint path
        endpoint = self._extract_endpoint(url)
        method = "POST"
        
        # Generate request ID
        request_id = str(uuid.uuid4())[:8]
        
        # Save pending request
        self.pending_requests[request_id] = {
            "method": method,
            "url": url,
            "time": start_time
        }
        
        # Extract JSON payload for logging
        payload = None
        if "json" in kwargs:
            payload = kwargs["json"]
            
        # Log detailed request information to file
        headers = kwargs.get("headers", {})
        detailed_logger.debug(
            f"\n=== REQUEST {request_id} [{method} {endpoint}] ===\n"
            f"URL: {url}\n"
            f"Headers: {json.dumps(headers, indent=2)}\n"
            f"Payload: {json.dumps(payload, indent=2) if payload else 'None'}\n"
        )
        
        try:
            # Execute request
            response = requests.post(url, **kwargs)
            
            # Calculate response time
            end_time = time.time()
            elapsed = end_time - start_time
            timing_str = self._format_timing(elapsed)
            
            # Get response info
            status_code = response.status_code
            content_type = response.headers.get("Content-Type", "")
            
            # Status indicator (green check for 2xx, yellow for others)
            if 200 <= status_code < 300:
                status_indicator = f"{GREEN}✓{RESET}"
            else:
                status_indicator = f"{YELLOW}⚠{RESET}"
            
            # Log response info
            http_logger.info(f"{method}  {status_indicator}  {GRAY}{endpoint}{RESET}  {timing_str}")
            
            # Log detailed response to file
            response_body = self._get_safe_response_body(response)
            detailed_logger.debug(
                f"\n=== RESPONSE {request_id} [{method} {endpoint}] ===\n"
                f"Status: {status_code}\n"
                f"Duration: {elapsed:.3f}s\n"
                f"Headers: {json.dumps(dict(response.headers), indent=2)}\n"
                f"Body: {response_body}\n"
                f"{'=' * 80}\n"
            )
            
            # Record metrics
            self.requests.append({
                "method": method,
                "endpoint": endpoint,
                "url": url,
                "status": status_code,
                "elapsed": elapsed,
                "time": start_time
            })
            
            # Clean up pending request
            if request_id in self.pending_requests:
                del self.pending_requests[request_id]
            
            # Raise for status to catch HTTP errors
            response.raise_for_status()
            
            return response
            
        except requests.exceptions.RequestException as e:
            # Calculate error response time
            end_time = time.time()
            elapsed = end_time - start_time
            timing_str = self._format_timing(elapsed)
            
            # Record error metrics
            status_code = None
            response_body = "No response"
            error_code = None
            error_message = None
            
            if hasattr(e, "response") and e.response:
                status_code = e.response.status_code
                response_body = self._get_safe_response_body(e.response)
                
                # Try to extract error code and message if JSON response
                try:
                    error_json = e.response.json()
                    if isinstance(error_json, dict):
                        error_code = error_json.get("code")
                        error_message = error_json.get("message", "")
                except:
                    pass
            
            # Build more informative error message
            error_info = f"Status: {status_code}"
            if error_code:
                error_info += f", Code: {error_code}"
            if error_message:
                error_info += f", Message: {error_message}"
                
            # Log error in simplified format
            http_logger.error(f"{method}  {RED}✗{RESET}  {GRAY}{endpoint}{RESET}  {timing_str}  {error_info}")
            
            # Log detailed error to file
            detailed_logger.error(
                f"\n=== ERROR RESPONSE {request_id} [{method} {endpoint}] ===\n"
                f"Status: {status_code}\n"
                f"Duration: {elapsed:.3f}s\n"
                f"Error Type: {type(e).__name__}\n"
                f"Error Message: {str(e)}\n"
                f"Response Body: {response_body}\n"
                f"{'=' * 80}\n"
            )
            
            # Log detailed error for DEBUG level
            logging.getLogger(__name__).error(
                f"[ERROR {method}] Status: {status_code}, Time: {elapsed:.3f}s, Endpoint: {endpoint}\n"
                f"Response: {response_body}"
            )
            
            # Record metrics
            self.requests.append({
                "method": method,
                "endpoint": endpoint,
                "url": url,
                "status": status_code,
                "elapsed": elapsed,
                "time": start_time,
                "error": True,
                "error_code": error_code,
                "error_message": error_message
            })
            
            # Clean up pending request
            if request_id in self.pending_requests:
                del self.pending_requests[request_id]
            
            # Re-raise the exception
            raise

    def _extract_endpoint(self, url: str) -> str:
        """Extract the endpoint path from a URL."""
        parts = url.split('/')
        if len(parts) <= 3:  # http://example.com has 3 parts
            return "/"
            
        # For http://localhost:8082/account/balance -> /account/balance
        return '/' + '/'.join(parts[3:])
        
    def _format_timing(self, elapsed: float) -> str:
        """Format request timing for display"""
        if elapsed < 1.0:
            return f"{elapsed*1000:.0f} ms"
        else:
            return f"{elapsed:.2f} s"

    def _get_safe_response_body(self, response: requests.Response) -> str:
        """
        Safely extract the response body as a string, handling potential errors.
        
        Args:
            response: The response object
            
        Returns:
            String representation of the response body
        """
        try:
            content_type = response.headers.get("Content-Type", "")
            if "application/json" in content_type:
                try:
                    return json.dumps(response.json(), indent=2)
                except:
                    return response.text or "Empty or invalid JSON"
            else:
                return response.text or "Empty response body"
        except Exception as e:
            return f"Error retrieving response body: {str(e)}"

    def get_request_stats(self) -> Dict[str, Any]:
        """Get statistics about the requests made."""
        if not self.requests:
            return {"total_requests": 0, "avg_time": 0, "endpoints": {}}
            
        total_time = sum(req["elapsed"] for req in self.requests)
        avg_time = total_time / len(self.requests)
        
        # Group by endpoint
        endpoints = {}
        for endpoint, times in self.request_times.items():
            endpoints[endpoint] = {
                "count": len(times),
                "avg_time": sum(times) / len(times),
                "min_time": min(times),
                "max_time": max(times)
            }
            
        # Count by status code
        status_counts = {}
        for req in self.requests:
            status = req.get("status", "unknown")
            if status not in status_counts:
                status_counts[status] = 0
            status_counts[status] += 1
            
        return {
            "total_requests": len(self.requests),
            "total_time": total_time,
            "avg_time": avg_time,
            "endpoints": endpoints,
            "status_counts": status_counts
        }

    def get_slowest_requests(self, limit: int = 5) -> List[Dict[str, Any]]:
        """Get the slowest requests."""
        if not self.requests:
            return []
            
        sorted_requests = sorted(self.requests, key=lambda r: r["elapsed"], reverse=True)
        return sorted_requests[:limit]

    def print_summary_report(self):
        """Print a summary report of the requests made."""
        stats = self.get_request_stats()
        if stats["total_requests"] == 0:
            logging.getLogger(__name__).debug("No API requests made.")
            return
            
        # Format the summary - more concise
        summary_lines = [
            f"API requests: {stats['total_requests']} total, {stats['avg_time']:.3f}s avg"
        ]
        
        # Add status code summary instead of endpoint details
        if "status_counts" in stats and stats["status_counts"]:
            status_info = []
            for status, count in sorted(stats["status_counts"].items()):
                status_color = GREEN if status and status < 400 else YELLOW if status and status < 500 else RED if status else GRAY
                status_info.append(f"{status_color}{status or 'unknown'}{RESET}:{count}")
            
            if status_info:
                summary_lines.append(f"  Status codes: {', '.join(status_info)}")
            
        # Only show single slowest request for DEBUG logs
        slow_requests = self.get_slowest_requests(1)
        if slow_requests:
            req = slow_requests[0]
            endpoint = req.get('endpoint', 'unknown')
            # Format for better readability
            time_str = f"{req['elapsed']*1000:.0f}ms" if req['elapsed'] < 1 else f"{req['elapsed']:.2f}s"
            summary_lines.append(f"  Slowest: {endpoint} ({time_str})")
        
        # Add log file location
        summary_lines.append(f"  Detailed logs: {self.log_file_path}")
                
        # Log the summary as a single message, not multiple lines
        logging.getLogger(__name__).debug("\n".join(summary_lines))
        
        # Also log summary to the detailed log file
        detailed_logger.info(f"Request summary: {stats['total_requests']} requests, {stats['avg_time']:.3f}s avg") 