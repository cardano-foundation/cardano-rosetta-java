#!/usr/bin/env python3
"""
Extract individual test cases from Postman collection into separate JSON files.
Preserves the entire collection folder structure.
"""

import json
import re
import argparse
from pathlib import Path


def snake_case(name):
    """Convert any string to snake_case for file naming."""
    # Convert to lowercase first
    name = name.lower()

    # Replace any non-alphanumeric character with space
    name = re.sub(r"[^a-z0-9]+", " ", name)

    # Strip and collapse multiple spaces
    name = " ".join(name.split())

    # Replace spaces with underscores
    name = name.replace(" ", "_")

    return name


def is_request_item(item):
    """Check if an item is a request (has a request field) vs a folder."""
    return "request" in item


def get_test_script_from_events(entity):
    """Extract the post-response (test) script text from an entity's events.

    Supports collection, folder, and request level `event` arrays. Returns a
    single string with newline-separated content, or None if absent.
    """
    events = entity.get("event") or []
    for ev in events:
        if isinstance(ev, dict) and ev.get("listen") == "test":
            script = ev.get("script")
            if isinstance(script, dict):
                exec_val = script.get("exec")
                if isinstance(exec_val, list):
                    return "\n".join(exec_val)
                if isinstance(exec_val, str):
                    return exec_val
            if isinstance(script, str):
                return script
    return None


def extract_test_case(test_item):
    """Extract test case data from a Postman request item."""
    test_name = test_item["name"]

    # Parse the request body JSON if it exists
    request_body = None
    if "body" in test_item["request"] and "raw" in test_item["request"]["body"]:
        request_body = json.loads(test_item["request"]["body"]["raw"])

    # Create the test case object
    test_case = {
        "test_name": test_name,
        "description": f"Test case: {test_name}",
        "request_body": request_body,
    }

    return test_case


def process_item(item, current_path):
    """
    Recursively process a Postman collection item.
    Returns the count of test cases processed.
    """
    count = 0

    if is_request_item(item):
        # This is a request, extract and save it
        try:
            test_case = extract_test_case(item)

            # Generate file name
            file_name = snake_case(item["name"]) + ".json"
            file_path = current_path / file_name

            # Write the test case to file with ensure_ascii=False to preserve Unicode
            with open(file_path, "w", encoding="utf-8") as f:
                json.dump(test_case, f, indent=2, ensure_ascii=False)

            # Export post-response (test) script if present
            script_text = get_test_script_from_events(item)
            if script_text:
                script_path = current_path / (snake_case(item["name"]) + ".tests.js")
                with open(script_path, "w", encoding="utf-8") as sf:
                    sf.write(script_text)

            rel_path = _safe_rel(file_path)
            print(f"  ✓ {item['name']} -> {rel_path}")
            count = 1

        except Exception as e:
            print(f"  ✗ Error processing {item['name']}: {e}")

    elif "item" in item:
        # This is a folder, create directory and process its items
        folder_name = snake_case(item["name"])
        folder_path = current_path / folder_name
        folder_path.mkdir(parents=True, exist_ok=True)

        # Export folder-level test script if present
        folder_script = get_test_script_from_events(item)
        if folder_script:
            with open(folder_path / "_tests.js", "w", encoding="utf-8") as fs:
                fs.write(folder_script)

        rel_path = _safe_rel(folder_path)
        print(f"\nProcessing folder: {item['name']} -> {rel_path}/")

        # Recursively process all items in this folder
        for sub_item in item["item"]:
            count += process_item(sub_item, folder_path)

    return count


def _safe_rel(path: Path) -> Path:
    """Safely make `path` relative to CWD if possible."""
    try:
        return path.relative_to(Path.cwd())
    except Exception:
        return path


def main():
    """Main extraction process."""
    # CLI
    parser = argparse.ArgumentParser(
        description="Extract request bodies and test scripts from a Postman collection",
    )
    parser.add_argument(
        "--input",
        default="rosetta-java.postman_collection.json",
        help="Input Postman collection JSON file",
    )
    parser.add_argument(
        "--output-dir",
        default="extracted_tests",
        help="Output directory for extracted files",
    )
    args = parser.parse_args()

    # Paths
    collection_path = Path(args.input)
    output_dir = Path(args.output_dir)

    # Load the Postman collection
    print("Loading Postman collection...")
    with open(collection_path, "r", encoding="utf-8") as f:
        collection = json.load(f)

    # Clean and recreate output directory
    if output_dir.exists():
        import shutil

        shutil.rmtree(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    # Get collection name for root folder
    collection_name = snake_case(collection["info"]["name"])
    root_path = output_dir / collection_name
    root_path.mkdir(parents=True, exist_ok=True)

    print(f"\nExtracting from collection: {collection['info']['name']}")
    print(f"Output directory: {root_path.absolute()}\n")

    # Export collection-level (root) test script if present
    root_script = get_test_script_from_events(collection)
    if root_script:
        with open(root_path / "_tests.js", "w", encoding="utf-8") as rs:
            rs.write(root_script)

    # Process all items in the collection
    total_tests = 0
    for item in collection["item"]:
        total_tests += process_item(item, root_path)

    print(f"\n{'=' * 50}")
    print("Extraction complete!")
    print(f"Total test cases extracted: {total_tests}")
    print(f"Output directory: {output_dir.absolute()}")

    # Show the created structure
    print(f"\n{'=' * 50}")
    print("Directory structure created:")

    def show_structure(path, prefix="", max_depth=3, current_depth=0):
        """Recursively show directory structure."""
        if current_depth >= max_depth:
            return

        items = sorted(path.iterdir())
        dirs = [i for i in items if i.is_dir()]
        [i for i in items if i.is_file() and i.suffix == ".json"]

        for d in dirs:
            print(f"{prefix}{d.name}/")
            file_count = len(list(d.glob("**/*.json")))
            if file_count > 0:
                print(f"{prefix}  ({file_count} JSON files)")
            show_structure(d, prefix + "  ", max_depth, current_depth + 1)

    show_structure(root_path, "  ")


if __name__ == "__main__":
    main()
