"""Initializer agent that scaffolds the Spring Boot project."""

import os

from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage

from .progress_tracker import ProgressTracker
from .security import bash_security_filter


PROMPT_PATH = os.path.join(os.path.dirname(__file__), "prompts", "initializer_prompt.md")


def load_prompt() -> str:
    """Load the initializer system prompt from file."""
    with open(PROMPT_PATH, "r") as f:
        return f.read()


async def run_initializer(tracker: ProgressTracker, project_dir: str) -> str | None:
    """Run the initializer agent to scaffold the project.

    Returns the session_id for potential follow-up sessions.
    """
    prompt = load_prompt()
    working_dir = project_dir

    print("\n=== Initializer Agent ===")
    print("Scaffolding Spring Boot project...")

    options = ClaudeAgentOptions(
        allowed_tools=["Read", "Write", "Edit", "Bash", "Glob", "Grep"],
        permission_mode="acceptEdits",
        max_turns=50,
        can_use_tool=bash_security_filter,
        cwd=working_dir,
    )

    session_id = None

    try:
        result: ResultMessage = await query(
            prompt=f"{prompt}\n\nWorking directory: {working_dir}\n\nPlease scaffold the entire project now.",
            options=options,
        )

        session_id = result.session_id if hasattr(result, "session_id") else None

        if hasattr(result, "total_cost_usd") and result.total_cost_usd:
            print(f"  Cost: ${result.total_cost_usd:.4f}")

        # Mark scaffold as completed
        tracker.update_feature("project-scaffold", "completed", "Scaffolded by initializer agent")
        print("Project scaffolding complete.")

    except Exception as e:
        print(f"Initializer agent error: {e}")
        tracker.update_feature("project-scaffold", "failed", str(e))

    return session_id
