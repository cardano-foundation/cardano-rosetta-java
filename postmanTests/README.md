# Postman Collection Test Case Extractor

This tool extracts individual test cases from a Postman collection JSON file into separate JSON files, preserving the complete folder hierarchy.

## Purpose

Convert a Postman collection (v2.1 format) into individual JSON test case files for easier version control, automated testing, and test case management.

## Requirements

- Python 3.6+
- No external dependencies (uses only Python standard library)

## Usage

### Extract Test Cases

The `extract_test_cases.py` script extracts individual test cases from a Postman collection:

```bash
# Default usage (expects rosetta-java.postman_collection.json, outputs to extracted_tests/)
python3 extract_test_cases.py

# Specify custom input and output paths
python3 extract_test_cases.py --input my_collection.json --output-dir my_tests/

# Show help
python3 extract_test_cases.py --help
```

**Options:**
- `--input INPUT`: Input Postman collection JSON file (default: `rosetta-java.postman_collection.json`)
- `--output-dir OUTPUT_DIR`: Output directory for extracted files (default: `extracted_tests/`)

The script will create the output directory containing all test cases organized in the same folder structure as your Postman collection.

## Output Structure

The script preserves the complete folder hierarchy from your Postman collection. For example:

```
extracted_tests/
└── rosetta_java/
    ├── construction/
    │   ├── preprocess/
    │   │   ├── simple_transactions/
    │   │   │   ├── 1_input_1_output.json
    │   │   │   ├── 1_input_10_outputs.json
    │   │   │   └── ...
    │   │   ├── complex_transactions/
    │   │   ├── native_assets/
    │   │   └── ...
    │   ├── derive/
    │   ├── hash/
    │   └── ...
    └── data/
        ├── network_list.json
        ├── network_status.json
        └── ...
```

## Test Case File Format

Each extracted JSON file contains:

```json
{
  "test_name": "Original test name from Postman",
  "description": "Test case: Original test name from Postman",
  "request_body": {
    // The complete request body from the Postman test
  }
}
```

## Features

- **Preserves Unicode**: Special characters like → are preserved correctly
- **Generic naming**: Converts any test name to snake_case filename automatically
- **Recursive processing**: Handles nested folder structures of any depth
- **Error handling**: Reports any test cases that fail to extract
- **No dependencies**: Uses only Python standard library

## File Naming Convention

Test names are converted to snake_case filenames:
- Special characters are removed
- Spaces become underscores
- Everything is lowercase
- Examples:
  - `"1 input → 1 output"` becomes `1_input_1_output.json`
  - `"(-) invalid address"` becomes `invalid_address.json`
  - `"REVIEW: test case"` becomes `review_test_case.json`

## Updating the Collection

### Bidirectional Sync

**Extract from Collection:**
```bash
python3 extract_test_cases.py
```

**Update Collection from Test Files:**

The `update_collection.py` script reads modified test files and updates the Postman collection:

```bash
# Basic update (default mode)
python3 update_collection.py

# Specify custom paths
python3 update_collection.py --input my_collection.json --output updated.json --test-dir my_tests/

# Show help
python3 update_collection.py --help
```

**Options:**
- `--mode {update,sync}`: Update mode (currently only `update` is implemented)
- `--input INPUT`: Input Postman collection file (default: `rosetta-java.postman_collection.json`)
- `--output OUTPUT`: Output updated collection file (default: `rosetta-java.postman_collection.updated.json`)
- `--test-dir TEST_DIR`: Directory containing extracted test files (default: `extracted_tests/`)

The script:
- Reads modified test files from the test directory
- Updates the Postman collection with changes
- Creates automatic timestamped backups
- Generates detailed log of all changes

### Workflow

1. **Initial Setup**: Export collection from Postman → Extract test files
2. **Make Changes**: Edit individual JSON files in `extracted_tests/`
3. **Update Collection**: Run `update_collection.py`
4. **Import Back**: Load updated collection into Postman

## Troubleshooting

If a test case fails to extract, the script will show an error message but continue processing other tests. Common issues:

- **Malformed JSON in request body**: Check the original Postman test for syntax errors
- **Missing request body**: Some test types may not have a body (e.g., GET requests)

The script will report the total number of test cases extracted at the end of execution.