"""Feature progress tracking for the coding agent loop."""

import json
import os
from datetime import datetime

FEATURE_LIST_PATH = "feature_list.json"
PROGRESS_FILE_PATH = "claude-progress.txt"


class ProgressTracker:
    """Manages feature_list.json and claude-progress.txt."""

    def __init__(self, base_dir: str = ".", default_features: list[dict] | None = None, service_name: str = "Service"):
        self.base_dir = base_dir
        self.feature_list_path = os.path.join(base_dir, FEATURE_LIST_PATH)
        self.progress_file_path = os.path.join(base_dir, PROGRESS_FILE_PATH)
        self.features: list[dict] = []
        self.default_features = default_features or []
        self.service_name = service_name

    def initialize_features(self) -> None:
        """Create the initial feature list if it doesn't exist."""
        if os.path.exists(self.feature_list_path):
            self.load_features()
            return
        self.features = [dict(f) for f in self.default_features]
        self._save()

    def load_features(self) -> list[dict]:
        """Load features from feature_list.json."""
        with open(self.feature_list_path, "r") as f:
            self.features = json.load(f)
        return self.features

    def load_features_by_id(self, feature_id: str) -> dict | None:
        """Get a single feature by ID."""
        for feature in self.features:
            if feature["id"] == feature_id:
                return feature
        return None

    def update_feature(self, feature_id: str, status: str, notes: str = "") -> None:
        """Update a feature's status and optional notes."""
        for feature in self.features:
            if feature["id"] == feature_id:
                feature["status"] = status
                feature["updated_at"] = datetime.now().isoformat()
                if notes:
                    feature["notes"] = notes
                break
        self._save()
        self.write_progress_file()

    def get_next_pending(self) -> dict | None:
        """Get the next feature with status 'pending'."""
        for feature in self.features:
            if feature["status"] == "pending":
                return feature
        return None

    def get_progress_summary(self) -> str:
        """Return a human-readable progress summary."""
        total = len(self.features)
        completed = sum(1 for f in self.features if f["status"] == "completed")
        in_progress = sum(1 for f in self.features if f["status"] == "in_progress")
        failed = sum(1 for f in self.features if f["status"] == "failed")
        pending = sum(1 for f in self.features if f["status"] == "pending")

        lines = [
            f"Progress: {completed}/{total} features completed",
            f"  Completed:   {completed}",
            f"  In Progress: {in_progress}",
            f"  Failed:      {failed}",
            f"  Pending:     {pending}",
            "",
        ]

        for feature in self.features:
            status_icon = {
                "completed": "[x]",
                "in_progress": "[~]",
                "failed": "[!]",
                "pending": "[ ]",
            }.get(feature["status"], "[?]")
            lines.append(f"  {status_icon} {feature['name']} ({feature['id']})")

        return "\n".join(lines)

    def write_progress_file(self) -> None:
        """Write human-readable progress to claude-progress.txt."""
        summary = self.get_progress_summary()
        header = f"# {self.service_name} - Build Progress\n# Updated: {datetime.now().isoformat()}\n\n"
        with open(self.progress_file_path, "w") as f:
            f.write(header + summary + "\n")

    def all_complete(self) -> bool:
        """Check if all features are completed."""
        return all(f["status"] == "completed" for f in self.features)

    def _save(self) -> None:
        """Persist features to feature_list.json."""
        with open(self.feature_list_path, "w") as f:
            json.dump(self.features, f, indent=2)
