"""Feature progress tracking for the coding agent loop."""

import json
import os
from datetime import datetime

FEATURE_LIST_PATH = "feature_list.json"
PROGRESS_FILE_PATH = "claude-progress.txt"

DEFAULT_FEATURES = [
    {"id": "project-scaffold", "name": "Project Scaffolding", "status": "pending", "description": "Create directory structure, pom.xml, and skeleton classes"},
    {"id": "database-entities", "name": "Database Entities", "status": "pending", "description": "Implement NarrationScript and ScriptSegment JPA entities"},
    {"id": "repositories", "name": "Repositories", "status": "pending", "description": "Implement Spring Data JPA repositories"},
    {"id": "dtos", "name": "DTOs", "status": "pending", "description": "Implement ScriptRequest, ScriptResponse, SegmentResponse"},
    {"id": "segmentation-logic", "name": "Segmentation Logic", "status": "pending", "description": "Implement sentence-boundary segmentation algorithm (~20 words/segment)"},
    {"id": "rest-controller", "name": "REST Controller", "status": "pending", "description": "Implement CRUD endpoints at /api/v1/scripts"},
    {"id": "exception-handling", "name": "Exception Handling", "status": "pending", "description": "Implement GlobalExceptionHandler and ResourceNotFoundException"},
    {"id": "spring-profiles", "name": "Spring Profiles", "status": "pending", "description": "Configure local (H2), dev (MySQL), prod profiles"},
    {"id": "flyway-migration", "name": "Flyway Migration", "status": "pending", "description": "Set up Flyway with V1__init_schema.sql"},
    {"id": "unit-tests-service", "name": "Service Unit Tests", "status": "pending", "description": "Write SegmentationServiceTest with Mockito"},
    {"id": "unit-tests-controller", "name": "Controller Unit Tests", "status": "pending", "description": "Write ScriptControllerTest with MockMvc"},
    {"id": "unit-tests-repository", "name": "Repository Unit Tests", "status": "pending", "description": "Write NarrationScriptRepositoryTest with H2"},
    {"id": "docker-config", "name": "Docker Configuration", "status": "pending", "description": "Create Dockerfile and docker-compose.yml"},
    {"id": "actuator-health", "name": "Actuator Health", "status": "pending", "description": "Configure Spring Boot Actuator health endpoint"},
]


class ProgressTracker:
    """Manages feature_list.json and claude-progress.txt."""

    def __init__(self, base_dir: str = "."):
        self.base_dir = base_dir
        self.feature_list_path = os.path.join(base_dir, FEATURE_LIST_PATH)
        self.progress_file_path = os.path.join(base_dir, PROGRESS_FILE_PATH)
        self.features: list[dict] = []

    def initialize_features(self) -> None:
        """Create the initial feature list if it doesn't exist."""
        if os.path.exists(self.feature_list_path):
            self.load_features()
            return
        self.features = [dict(f) for f in DEFAULT_FEATURES]
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
        header = f"# Narration Segmentation Service - Build Progress\n# Updated: {datetime.now().isoformat()}\n\n"
        with open(self.progress_file_path, "w") as f:
            f.write(header + summary + "\n")

    def all_complete(self) -> bool:
        """Check if all features are completed."""
        return all(f["status"] == "completed" for f in self.features)

    def _save(self) -> None:
        """Persist features to feature_list.json."""
        with open(self.feature_list_path, "w") as f:
            json.dump(self.features, f, indent=2)
