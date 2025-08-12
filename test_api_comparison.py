#!/usr/bin/env python3
"""
Database Snapshot Validation Script

This script validates the integrity and completeness of database snapshots by comparing
Cardano Rosetta API responses between:
- Production environment (port 8082) with live/original database
- Test environment (port 8083) with restored snapshot database

Purpose:
- Verify that snapshot restoration produces identical API responses to production
- Validate data integrity after database restore from compressed snapshots
- Test compatibility between original and restored database states
- Ensure no data loss during snapshot creation, compression, and restoration process

The script samples every 1000th block to provide comprehensive coverage while
maintaining reasonable execution time. Any differences indicate potential issues
with the snapshot process that need investigation.

Usage:
- Run after setting up test environment with restored snapshot
- Ensure both production and test APIs are running and accessible
- Review output for any API response mismatches or errors

Configuration:
- PROD_API_URL: Production Rosetta API endpoint
- TEST_API_URL: Test Rosetta API endpoint (restored from snapshot)
- Block range and sampling configured below
"""

import requests
import json
import logging
import time
from datetime import datetime
from typing import Dict, Any, Optional, Tuple

# Configuration
PROD_API_URL = "http://localhost:8082/block"
TEST_API_URL = "http://localhost:8083/block"
START_BLOCK = 1
END_BLOCK = 3766257
BLOCK_STEP = 1000
TIMEOUT = 30
MAX_RETRIES = 3
LOG_FILE = "api_compare.log"

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(LOG_FILE),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

def make_request(url: str, block_index: int, retries: int = MAX_RETRIES) -> Optional[Dict[Any, Any]]:
    """Make API request with retry logic"""
    payload = {
        "network_identifier": {
            "blockchain": "cardano",
            "network": "preprod"
        },
        "block_identifier": {
            "index": block_index
        }
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    for attempt in range(retries):
        try:
            response = requests.post(
                url, 
                json=payload, 
                headers=headers, 
                timeout=TIMEOUT
            )
            
            if response.status_code == 200:
                return response.json()
            else:
                logger.warning(f"Block {block_index}: HTTP {response.status_code} from {url} (attempt {attempt + 1})")
                if attempt == retries - 1:
                    return {"error": f"HTTP {response.status_code}", "response": response.text}
                    
        except requests.exceptions.RequestException as e:
            logger.warning(f"Block {block_index}: Request failed to {url} (attempt {attempt + 1}): {str(e)}")
            if attempt == retries - 1:
                return {"error": "RequestException", "details": str(e)}
        
        if attempt < retries - 1:
            time.sleep(1)  # Wait 1 second before retry
    
    return None

def compare_responses(prod_response: Optional[Dict], test_response: Optional[Dict]) -> Tuple[bool, str]:
    """Compare two API responses"""
    # Both failed
    if prod_response is None and test_response is None:
        return True, "Both APIs failed"
    
    # One failed, one succeeded
    if prod_response is None:
        return False, f"Prod failed, Test succeeded: {json.dumps(test_response, indent=2)}"
    
    if test_response is None:
        return False, f"Test failed, Prod succeeded: {json.dumps(prod_response, indent=2)}"
    
    # Both have errors
    if "error" in prod_response and "error" in test_response:
        return prod_response == test_response, "Both have errors"
    
    # One has error, one doesn't
    if "error" in prod_response and "error" not in test_response:
        return False, f"Prod error: {json.dumps(prod_response, indent=2)}\nTest success: {json.dumps(test_response, indent=2)}"
    
    if "error" in test_response and "error" not in prod_response:
        return False, f"Test error: {json.dumps(test_response, indent=2)}\nProd success: {json.dumps(prod_response, indent=2)}"
    
    # Both successful - compare JSON
    if prod_response == test_response:
        return True, "Match"
    else:
        return False, f"Data mismatch:\nProd: {json.dumps(prod_response, indent=2)}\nTest: {json.dumps(test_response, indent=2)}"

def test_block(block_index: int) -> None:
    """Test a single block against both APIs"""
    logger.info(f"Testing block {block_index}")
    
    # Make requests to both APIs
    prod_response = make_request(PROD_API_URL, block_index)
    test_response = make_request(TEST_API_URL, block_index)
    
    # Compare responses
    is_match, details = compare_responses(prod_response, test_response)
    
    # Log result
    result = "Yes" if is_match else "No"
    logger.info(f"Block {block_index}: Match={result}")
    
    if not is_match:
        logger.error(f"Block {block_index} MISMATCH:\n{details}")

def main():
    """Main execution function"""
    start_time = datetime.now()
    logger.info(f"Starting API comparison test")
    logger.info(f"Production API: {PROD_API_URL}")
    logger.info(f"Test API: {TEST_API_URL}")
    logger.info(f"Block range: {START_BLOCK} to {END_BLOCK} (every {BLOCK_STEP}th block)")
    logger.info(f"Timeout: {TIMEOUT}s, Max retries: {MAX_RETRIES}")
    logger.info("=" * 60)
    
    total_blocks = 0
    matched_blocks = 0
    mismatched_blocks = 0
    
    # Test every 1000th block
    for block_index in range(START_BLOCK, END_BLOCK + 1, BLOCK_STEP):
        try:
            test_block(block_index)
            total_blocks += 1
            
            # Progress logging every 1000 blocks tested
            if total_blocks % 100 == 0:
                elapsed = datetime.now() - start_time
                logger.info(f"Progress: {total_blocks} blocks tested in {elapsed}")
                
        except KeyboardInterrupt:
            logger.info("Test interrupted by user")
            break
        except Exception as e:
            logger.error(f"Unexpected error testing block {block_index}: {str(e)}")
            continue
    
    # Final summary
    end_time = datetime.now()
    elapsed = end_time - start_time
    
    logger.info("=" * 60)
    logger.info(f"Test completed in {elapsed}")
    logger.info(f"Total blocks tested: {total_blocks}")
    logger.info(f"Results logged to: {LOG_FILE}")

if __name__ == "__main__":
    main()