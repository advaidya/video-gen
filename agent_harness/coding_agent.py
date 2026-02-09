"""Coding agent that iteratively implements features."""

import os

from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage

from .progress_tracker import ProgressTracker
from .security import bash_security_filter


PROMPT_PATH = os.path.join(os.path.dirname(__file__), "prompts", "coding_prompt.md")
MAX_RETRIES = 3


def load_prompt() -> str:
    """Load the coding system prompt from file."""
    with open(PROMPT_PATH, "r") as f:
        return f.read()


async def run_coding_session(
    tracker: ProgressTracker,
    feature: dict,
    project_dir: str,
    previous_session_id: str | None = None,
) -> tuple[str | None, bool]:
    """Run a single coding session for one feature.

    Returns (session_id, success).
    """
    prompt = load_prompt()

    feature_instruction = (
        f"Your assigned feature is: {feature['id']} - {feature['name']}\n"
        f"Description: {feature['description']}\n\n"
        f"Working directory: {project_dir}\n\n"
        f"Please implement this feature now."
    )

    options = ClaudeAgentOptions(
        allowed_tools=["Read", "Write", "Edit", "Bash", "Glob", "Grep"],
        permission_mode="acceptEdits",
        max_turns=30,
        can_use_tool=bash_security_filter,
        cwd=project_dir,
    )

    # Support resuming from a previous session for fix-up cycles
    resume_id = previous_session_id if previous_session_id else None

    session_id = None
    success = False

    try:
        tracker.update_feature(feature["id"], "in_progress")

        result: ResultMessage = await query(
            prompt=f"{prompt}\n\n{feature_instruction}",
            options=options,
            resume=resume_id,
        )

        session_id = result.session_id if hasattr(result, "session_id") else None

        if hasattr(result, "total_cost_usd") and result.total_cost_usd:
            print(f"  Cost: ${result.total_cost_usd:.4f}")

        tracker.update_feature(feature["id"], "completed", f"Implemented by coding agent")
        success = True
        print(f"  Feature '{feature['name']}' completed.")

    except Exception as e:
        print(f"  Error implementing '{feature['name']}': {e}")
        tracker.update_feature(feature["id"], "failed", str(e))

    return session_id, success


async def run_coding_loop(tracker: ProgressTracker, project_dir: str) -> None:
    """Iterate over pending features and run coding sessions."""
    prompt = load_prompt()

    print("\n=== Coding Agent Loop ===")

    retry_counts: dict[str, int] = {}

    while not tracker.all_complete():
        feature = tracker.get_next_pending()
        if feature is None:
            # Check for failed features that can be retried
            failed = [f for f in tracker.features if f["status"] == "failed"]
            retryable = [f for f in failed if retry_counts.get(f["id"], 0) < MAX_RETRIES]

            if not retryable:
                print("No more features to process.")
                break

            feature = retryable[0]
            feature["status"] = "pending"
            retry_counts[feature["id"]] = retry_counts.get(feature["id"], 0) + 1
            print(f"  Retrying '{feature['name']}' (attempt {retry_counts[feature['id']]})")

        print(f"\nImplementing: {feature['name']} ({feature['id']})")

        session_id, success = await run_coding_session(
            tracker=tracker,
            feature=feature,
            project_dir=project_dir,
        )

        # Print progress
        print(tracker.get_progress_summary())

    print("\n=== Coding Agent Loop Complete ===")
    print(tracker.get_progress_summary())
