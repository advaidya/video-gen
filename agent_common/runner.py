"""Generic agent runner logic for initializer and coding sessions."""

import os

from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage

from .progress_tracker import ProgressTracker
from .security import bash_security_filter


MAX_RETRIES = 3


def _load_prompt(prompt_path: str) -> str:
    """Load a prompt from file."""
    with open(prompt_path, "r") as f:
        return f.read()


async def _collect_result(async_iter) -> ResultMessage | None:
    """Consume the query async iterator and return the final ResultMessage."""
    result = None
    async for message in async_iter:
        if isinstance(message, ResultMessage):
            result = message
    return result


async def run_initializer(tracker: ProgressTracker, project_dir: str, prompt_path: str) -> None:
    """Run the initializer agent to scaffold the project."""
    prompt = _load_prompt(prompt_path)

    print("\n=== Initializer Agent ===")
    print("Scaffolding project...")

    options = ClaudeAgentOptions(
        allowed_tools=["Read", "Write", "Edit", "Bash", "Glob", "Grep"],
        permission_mode="acceptEdits",
        max_turns=50,
        can_use_tool=bash_security_filter,
        cwd=project_dir,
    )

    try:
        result = await _collect_result(query(
            prompt=f"{prompt}\n\nWorking directory: {project_dir}\n\nPlease scaffold the entire project now.",
            options=options,
        ))

        if result and result.total_cost_usd:
            print(f"  Cost: ${result.total_cost_usd:.4f}")

        tracker.update_feature("project-scaffold", "completed", "Scaffolded by initializer agent")
        print("Project scaffolding complete.")

    except Exception as e:
        print(f"Initializer agent error: {e}")
        tracker.update_feature("project-scaffold", "failed", str(e))


async def run_coding_session(
    tracker: ProgressTracker,
    feature: dict,
    project_dir: str,
    prompt_path: str,
) -> bool:
    """Run a single coding session for one feature.

    Returns True on success.
    """
    prompt = _load_prompt(prompt_path)

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

    try:
        tracker.update_feature(feature["id"], "in_progress")

        result = await _collect_result(query(
            prompt=f"{prompt}\n\n{feature_instruction}",
            options=options,
        ))

        if result and result.total_cost_usd:
            print(f"  Cost: ${result.total_cost_usd:.4f}")

        tracker.update_feature(feature["id"], "completed", "Implemented by coding agent")
        print(f"  Feature '{feature['name']}' completed.")
        return True

    except Exception as e:
        print(f"  Error implementing '{feature['name']}': {e}")
        tracker.update_feature(feature["id"], "failed", str(e))
        return False


async def run_coding_loop(tracker: ProgressTracker, project_dir: str, prompt_path: str) -> None:
    """Iterate over pending features and run coding sessions."""
    print("\n=== Coding Agent Loop ===")

    retry_counts: dict[str, int] = {}

    while not tracker.all_complete():
        feature = tracker.get_next_pending()
        if feature is None:
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

        success = await run_coding_session(
            tracker=tracker,
            feature=feature,
            project_dir=project_dir,
            prompt_path=prompt_path,
        )

        print(tracker.get_progress_summary())

    print("\n=== Coding Agent Loop Complete ===")
    print(tracker.get_progress_summary())
