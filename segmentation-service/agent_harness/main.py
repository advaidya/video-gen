"""Orchestrator entry point for the segmentation-service agent harness."""

import asyncio
import os
import sys

# Allow running directly: python3 segmentation-service/agent_harness/main.py
_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
_PROJECT_ROOT = os.path.dirname(os.path.dirname(_SCRIPT_DIR))
if _PROJECT_ROOT not in sys.path:
    sys.path.insert(0, _PROJECT_ROOT)
if _SCRIPT_DIR not in sys.path:
    sys.path.insert(0, _SCRIPT_DIR)

from dotenv import load_dotenv

from agent_common.progress_tracker import ProgressTracker
from agent_common.runner import run_initializer, run_coding_loop
from features import FEATURES


SERVICE_NAME = "Narration Segmentation Service"
PROMPTS_DIR = os.path.join(os.path.dirname(__file__), "prompts")


async def main() -> None:
    """Main entry point for the agent-driven development pipeline."""
    load_dotenv()

    api_key = os.getenv("ANTHROPIC_API_KEY")
    if not api_key:
        print("Error: ANTHROPIC_API_KEY not set. Copy .env.example to .env and add your key.")
        sys.exit(1)

    # Project dir is the segmentation-service/ directory (parent of agent_harness/)
    project_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

    print("=" * 60)
    print(f"  {SERVICE_NAME} - Agent Builder")
    print("=" * 60)
    print(f"Project directory: {project_dir}")

    tracker = ProgressTracker(
        base_dir=project_dir,
        default_features=FEATURES,
        service_name=SERVICE_NAME,
    )
    tracker.initialize_features()

    # Phase 1: Run initializer if scaffold not done
    scaffold = tracker.load_features_by_id("project-scaffold")
    if scaffold and scaffold["status"] != "completed":
        print("\nPhase 1: Project Scaffolding")
        initializer_prompt = os.path.join(PROMPTS_DIR, "initializer_prompt.md")
        await run_initializer(tracker, project_dir, initializer_prompt)
    else:
        print("\nPhase 1: Scaffolding already complete, skipping.")

    # Phase 2: Run coding agent loop for remaining features
    if not tracker.all_complete():
        print("\nPhase 2: Feature Implementation")
        coding_prompt = os.path.join(PROMPTS_DIR, "coding_prompt.md")
        await run_coding_loop(tracker, project_dir, coding_prompt)
    else:
        print("\nAll features already complete!")

    # Final summary
    print("\n" + "=" * 60)
    print("  Final Progress Summary")
    print("=" * 60)
    print(tracker.get_progress_summary())
    tracker.write_progress_file()


if __name__ == "__main__":
    asyncio.run(main())
