#!/usr/bin/env python3
"""
Update Postman collection from extracted files.
Reverses the extraction process, applying changes from per-request JSON files
and `.tests.js` scripts (request, folder, and collection level) back to the
collection JSON.
"""

import json
import re
import shutil
import argparse
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Optional


class CollectionUpdater:
    """Handles updating Postman collection from individual test files/scripts."""

    def __init__(
        self,
        collection_path: str,
        test_dir: str,
        output_path: str,
        mode: str = "update",
    ):
        self.collection_path = Path(collection_path)
        self.test_dir = Path(test_dir)
        self.output_path = Path(output_path)
        self.mode = mode

        # Statistics tracking
        self.stats = {
            "total_files": 0,
            "updated": 0,
            "not_found": 0,
            "errors": 0,
            "skipped": 0,
            "collection_without_file": 0,
            "scripts_updated": 0,
        }

        # Detailed logs
        self.update_log = []  # request body updates
        self.error_log = []
        self.not_found_log = []
        self.collection_orphans = []
        self.script_update_log = []  # request/folder/collection script updates
        self.skipped_log = []  # per-file body skips
        self._folder_scripts_found = 0

    def snake_case(self, name: str) -> str:
        """Convert any string to snake_case for file naming (same as extraction script)."""
        # Convert to lowercase first
        name = name.lower()

        # Replace any non-alphanumeric character with space
        name = re.sub(r"[^a-z0-9]+", " ", name)

        # Strip and collapse multiple spaces
        name = " ".join(name.split())

        # Replace spaces with underscores
        name = name.replace(" ", "_")

        return name

    def load_collection(self) -> Dict:
        """Load the Postman collection from file."""
        print(f"Loading collection from: {self.collection_path}")
        with open(self.collection_path, "r", encoding="utf-8") as f:
            return json.load(f)

    def save_collection(self, collection: Dict) -> None:
        """Save the updated collection to file."""
        print(f"Saving updated collection to: {self.output_path}")
        with open(self.output_path, "w", encoding="utf-8") as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)

    def create_backup(self) -> Path:
        """Create a timestamped backup of the original collection."""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        backup_path = self.collection_path.with_suffix(f".backup_{timestamp}.json")
        print(f"Creating backup: {backup_path}")
        shutil.copy2(self.collection_path, backup_path)
        return backup_path

    def load_test_file(self, file_path: Path) -> Optional[Dict]:
        """Load and validate a test file."""
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                data = json.load(f)

            # Validate required fields
            if "test_name" not in data:
                self.error_log.append(f"Missing 'test_name' in {file_path}")
                return None

            return data

        except json.JSONDecodeError as e:
            self.error_log.append(f"Invalid JSON in {file_path}: {e}")
            return None
        except Exception as e:
            self.error_log.append(f"Error reading {file_path}: {e}")
            return None

    def find_item_in_collection(
        self, items: List[Dict], test_name: str, path: List[str]
    ) -> Optional[Dict]:
        """
        Navigate collection structure to find the matching test item.
        Returns the item reference if found, None otherwise.
        """
        for item in items:
            if "request" in item and item.get("name") == test_name:
                # This is a request item with matching name
                return item
            elif "item" in item and len(path) > 0:
                # This is a folder, check if it matches the next path segment
                folder_name = self.snake_case(item.get("name", ""))
                if folder_name == path[0]:
                    # Recurse into this folder
                    return self.find_item_in_collection(
                        item["item"], test_name, path[1:]
                    )

        return None

    def find_folder_in_collection(
        self, items: List[Dict], path: List[str]
    ) -> Optional[Dict]:
        """Find a folder (item group) in the collection by snake_case path parts."""
        if not path:
            return None
        current_items = items
        current_folder = None
        for part in path:
            found = None
            for it in current_items:
                if "item" in it:  # folder
                    if self.snake_case(it.get("name", "")) == part:
                        found = it
                        break
            if not found:
                return None
            current_folder = found
            current_items = found.get("item", [])
        return current_folder

    def _find_test_event(self, entity: Dict) -> Optional[Dict]:
        events = entity.get("event") or []
        for ev in events:
            if isinstance(ev, dict) and ev.get("listen") == "test":
                return ev
        return None

    def _ensure_test_event(self, entity: Dict) -> Dict:
        """Ensure entity has an event entry for tests; return that event dict."""
        events = entity.setdefault("event", [])
        ev = self._find_test_event(entity)
        if ev is None:
            ev = {"listen": "test", "script": {"type": "text/javascript", "exec": []}}
            events.append(ev)
        if "script" not in ev or not isinstance(ev["script"], dict):
            ev["script"] = {"type": "text/javascript", "exec": []}
        return ev

    def _current_script_text(self, entity: Dict) -> Optional[str]:
        ev = self._find_test_event(entity)
        if not ev:
            return None
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

    def update_test_script_on_entity(self, entity: Dict, script_text: str) -> bool:
        """Set/update the post-response (test) script on an entity.

        Returns True if updated (content changed), False if no change or invalid.
        """
        try:
            if not isinstance(script_text, str):
                return False
            current_text = self._current_script_text(entity) or ""
            new_text = script_text
            if current_text == new_text:
                return False  # no change

            # Only create/update event if there is new content or an existing event
            if new_text.strip() == "" and self._find_test_event(entity) is None:
                return False

            event = self._ensure_test_event(entity)
            script_obj = event["script"]
            # Preserve type (string vs list) if present; default to list of lines
            if isinstance(script_obj.get("exec"), list):
                new_exec = new_text.splitlines()
            elif isinstance(script_obj.get("exec"), str):
                new_exec = new_text
            else:
                # default to list of lines
                new_exec = new_text.splitlines()
            script_obj["exec"] = new_exec
            if "type" not in script_obj:
                script_obj["type"] = "text/javascript"
            self.stats["scripts_updated"] += 1
            return True
        except Exception as e:
            self.error_log.append(f"Error updating test script: {e}")
            return False

    def update_request_body(self, item: Dict, test_data: Dict) -> bool:
        """
        Update the request body while preserving all other fields.
        Returns True if updated (content changed), False if skipped/no-change.
        """
        try:
            # Check if there's a request body to update
            if "request_body" not in test_data or test_data["request_body"] is None:
                # No request body in test file (might be a GET request)
                return False

            # Ensure the item has the proper structure
            if "request" not in item:
                return False

            # Compare current vs new semantically
            current_raw = None
            if "body" in item["request"]:
                current_raw = item["request"]["body"].get("raw")

            def _parse_json(s):
                try:
                    return json.loads(s) if isinstance(s, str) else None
                except Exception:
                    return None

            current_obj = _parse_json(current_raw)
            new_obj = test_data["request_body"]

            if current_obj == new_obj:
                return False

            # Ensure body structure exists and set new raw
            if "body" not in item["request"]:
                item["request"]["body"] = {
                    "mode": "raw",
                    "options": {"raw": {"language": "json"}},
                }

            item["request"]["body"]["raw"] = json.dumps(
                new_obj, indent=2, ensure_ascii=False
            )

            return True

        except Exception as e:
            self.error_log.append(f"Error updating request body: {e}")
            return False

    def process_test_file(self, file_path: Path, collection: Dict) -> None:
        """Process a single test file and update the collection."""
        self.stats["total_files"] += 1

        # Load the test file
        test_data = self.load_test_file(file_path)
        if test_data is None:
            self.stats["errors"] += 1
            return

        test_name = test_data["test_name"]

        # Build the path from the file location
        # Remove the test_dir and collection name prefix
        relative_path = file_path.relative_to(self.test_dir)
        path_parts = list(
            relative_path.parts[1:-1]
        )  # Skip collection name and filename

        # Find the item in the collection
        item = self.find_item_in_collection(collection["item"], test_name, path_parts)

        if item is None:
            self.stats["not_found"] += 1
            self.not_found_log.append(
                {
                    "file": str(file_path),
                    "test_name": test_name,
                    "path": "/".join(path_parts),
                }
            )
            print(f"  ✗ Not found in collection: {test_name}")
            return

        # Update the request body
        if self.update_request_body(item, test_data):
            self.stats["updated"] += 1
            self.update_log.append(
                {
                    "file": str(file_path),
                    "test_name": test_name,
                    "status": "updated",
                    "action": "body",
                }
            )
            print(f"  ✓ Updated body: {test_name}")
        else:
            self.stats["skipped"] += 1
            self.skipped_log.append(
                {
                    "file": str(file_path),
                    "test_name": test_name,
                    "reason": "body_no_change",
                }
            )
            print(f"  - Skipped body (no change): {test_name}")

        # Update request-level test script if sibling .tests.js exists
        script_path = file_path.with_suffix(".tests.js")
        if script_path.exists():
            try:
                with open(script_path, "r", encoding="utf-8") as sf:
                    script_text = sf.read()
                if script_text is not None:
                    if self.update_test_script_on_entity(item, script_text):
                        print(f"    • Script updated: {script_path}")
                        self.script_update_log.append(
                            {
                                "scope": "request",
                                "file": str(file_path),
                                "script": str(script_path),
                                "test_name": test_name,
                            }
                        )
            except Exception as e:
                self.error_log.append(f"Error reading script {script_path}: {e}")

    def traverse_test_files(self) -> List[Path]:
        """Recursively find all test JSON files."""
        test_files = []
        for file_path in self.test_dir.rglob("*.json"):
            test_files.append(file_path)
        return sorted(test_files)

    def traverse_folder_scripts(self) -> List[Path]:
        """Find all folder-level `_tests.js` scripts including collection root."""
        return sorted(self.test_dir.rglob("_tests.js"))

    def mark_collection_items(self, items: List[Dict], path: str = "") -> None:
        """Mark all items in collection to track which ones have test files."""
        for item in items:
            if "request" in item:
                # This is a request item
                (f"{path}/{item.get('name', '')}" if path else item.get("name", ""))
                item["_checked"] = False  # Will be set to True when matched with a file
            elif "item" in item:
                # This is a folder, recurse
                folder_path = (
                    f"{path}/{item.get('name', '')}" if path else item.get("name", "")
                )
                self.mark_collection_items(item["item"], folder_path)

    def find_unchecked_items(self, items: List[Dict], path: str = "") -> None:
        """Find all items in collection that weren't matched with test files."""
        for item in items:
            if "request" in item:
                item_path = (
                    f"{path}/{item.get('name', '')}" if path else item.get("name", "")
                )
                if not item.get("_checked", False):
                    self.collection_orphans.append(item_path)
                    self.stats["collection_without_file"] += 1
                # Clean up the temporary marker
                item.pop("_checked", None)
            elif "item" in item:
                folder_path = (
                    f"{path}/{item.get('name', '')}" if path else item.get("name", "")
                )
                self.find_unchecked_items(item["item"], folder_path)

    def run(self) -> None:
        """Execute the update process."""
        print("=" * 60)
        print("Postman Collection Updater")
        print("=" * 60)

        # Create backup only when updating in-place
        in_place = False
        try:
            in_place = self.collection_path.resolve() == self.output_path.resolve()
        except Exception:
            in_place = str(self.collection_path) == str(self.output_path)
        if in_place:
            backup_path = self.create_backup()
            print(f"Backup created: {backup_path}\n")

        # Load collection
        collection = self.load_collection()
        collection_name = collection["info"]["name"]
        print(f"Collection: {collection_name}")
        print(f"Test directory: {self.test_dir}\n")

        # Mark all items for tracking
        self.mark_collection_items(collection["item"])

        # Sync folder and collection-level test scripts first
        folder_scripts = self.traverse_folder_scripts()
        self._folder_scripts_found = len(folder_scripts)
        if folder_scripts:
            print(f"Found {len(folder_scripts)} folder/collection scripts\n")
            for script_path in folder_scripts:
                rel = script_path.relative_to(self.test_dir)
                parts = list(rel.parts)
                # parts[0] is collection root folder; remaining are folder path
                folder_parts = parts[1:-1]  # exclude collection root and filename
                try:
                    with open(script_path, "r", encoding="utf-8") as f:
                        content = f.read()
                except Exception as e:
                    self.error_log.append(
                        f"Error reading folder script {script_path}: {e}"
                    )
                    continue
                if not folder_parts:
                    # collection-level script on root collection object
                    if self.update_test_script_on_entity(collection, content):
                        print(f"  ✓ Root script updated: {script_path}")
                        self.script_update_log.append(
                            {"scope": "collection", "script": str(script_path)}
                        )
                else:
                    folder_item = self.find_folder_in_collection(
                        collection["item"], folder_parts
                    )
                    if folder_item:
                        if self.update_test_script_on_entity(folder_item, content):
                            print(f"  ✓ Folder script updated: {script_path}")
                            self.script_update_log.append(
                                {
                                    "scope": "folder",
                                    "path": "/".join(folder_parts),
                                    "script": str(script_path),
                                }
                            )
                    else:
                        self.not_found_log.append(
                            {
                                "folder_script": str(script_path),
                                "path": "/".join(folder_parts),
                            }
                        )
                        print(f"  ✗ Folder not found for script: {script_path}")

        # Find and process all test files
        test_files = self.traverse_test_files()
        print(f"Found {len(test_files)} test files\n")

        print("Processing test files:")
        print("-" * 40)

        for file_path in test_files:
            # When we match an item, mark it as checked
            relative_path = file_path.relative_to(self.test_dir)
            path_parts = list(relative_path.parts[1:-1])

            # Load test file to get the name
            test_data = self.load_test_file(file_path)
            if test_data:
                test_name = test_data["test_name"]
                item = self.find_item_in_collection(
                    collection["item"], test_name, path_parts
                )
                if item:
                    item["_checked"] = True

            # Process the file
            self.process_test_file(file_path, collection)

        # Find items without test files
        self.find_unchecked_items(collection["item"])

        # Save the updated collection
        print("\n" + "-" * 40)
        self.save_collection(collection)

        # Print summary
        self.print_summary()

        # Save detailed log
        self.save_log()

    def print_summary(self) -> None:
        """Print a summary of the update process."""
        print("\n" + "=" * 60)
        print("UPDATE SUMMARY")
        print("=" * 60)
        print(f"Total test files processed: {self.stats['total_files']}")
        print(f"  ✓ Successfully updated: {self.stats['updated']}")
        print(f"  - Skipped (no change): {self.stats['skipped']}")
        print(f"  ✗ Not found in collection: {self.stats['not_found']}")
        print(f"  ✗ Errors: {self.stats['errors']}")
        print(f"  ✓ Scripts synced: {self.stats['scripts_updated']}")
        print(
            f"\nCollection items without test files: {self.stats['collection_without_file']}"
        )

        if self.not_found_log:
            print("\n" + "-" * 40)
            print("Test files not found in collection:")
            for entry in self.not_found_log[:5]:  # Show first 5
                print(f"  - {entry['test_name']} ({entry['path']})")
            if len(self.not_found_log) > 5:
                print(f"  ... and {len(self.not_found_log) - 5} more")

        if self.collection_orphans:
            print("\n" + "-" * 40)
            print("Collection items without test files:")
            for path in self.collection_orphans[:5]:  # Show first 5
                print(f"  - {path}")
            if len(self.collection_orphans) > 5:
                print(f"  ... and {len(self.collection_orphans) - 5} more")

        if self.error_log:
            print("\n" + "-" * 40)
            print("Errors encountered:")
            for error in self.error_log[:5]:  # Show first 5
                print(f"  - {error}")
            if len(self.error_log) > 5:
                print(f"  ... and {len(self.error_log) - 5} more")

    def save_log(self) -> None:
        """Save detailed log to file as <output-stem>.log.json."""
        base = self.output_path.with_suffix("")
        log_file = base.parent / (base.name + ".log.json")
        log_data = {
            "timestamp": datetime.now().isoformat(),
            "statistics": self.stats,
            "updates": self.update_log,
            "not_found": self.not_found_log,
            "collection_orphans": self.collection_orphans,
            "errors": self.error_log,
            "script_updates": self.script_update_log,
            "skipped_details": self.skipped_log,
            "folder_scripts_found": self._folder_scripts_found,
        }

        with open(log_file, "w", encoding="utf-8") as f:
            json.dump(log_data, f, indent=2, ensure_ascii=False)

        print(f"\nDetailed log saved to: {log_file}")


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="Update Postman collection from test files",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Basic update (default mode)
  python3 update_collection.py
  
  # Specify custom paths
  python3 update_collection.py --input my_collection.json --output updated.json --test-dir my_tests/
  
  # Dry run to preview changes (not implemented yet)
  python3 update_collection.py --dry-run
        """,
    )

    parser.add_argument(
        "--mode",
        choices=["update", "sync"],
        default="update",
        help="Update mode: update (existing only) or sync (add new tests - not implemented)",
    )

    parser.add_argument(
        "--input",
        default="rosetta-java.postman_collection.json",
        help="Input Postman collection file",
    )

    parser.add_argument(
        "--output",
        default="rosetta-java.postman_collection.updated.json",
        help="Output updated collection file",
    )

    parser.add_argument(
        "--test-dir",
        default="extracted_tests",
        help="Directory containing extracted test files",
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Preview changes without applying them (not implemented yet)",
    )

    args = parser.parse_args()

    # Check if input files exist
    if not Path(args.input).exists():
        print(f"Error: Collection file not found: {args.input}")
        return 1

    if not Path(args.test_dir).exists():
        print(f"Error: Test directory not found: {args.test_dir}")
        return 1

    if args.dry_run:
        print("Note: --dry-run mode is not implemented yet")
        return 1

    if args.mode == "sync":
        print("Note: sync mode is not implemented yet, using update mode")
        args.mode = "update"

    # Create and run the updater
    updater = CollectionUpdater(
        collection_path=args.input,
        test_dir=args.test_dir,
        output_path=args.output,
        mode=args.mode,
    )

    try:
        updater.run()
        return 0
    except Exception as e:
        print(f"\nFatal error: {e}")
        import traceback

        traceback.print_exc()
        return 1


if __name__ == "__main__":
    exit(main())
