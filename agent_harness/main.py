"""Orchestrator entry point for the agent harness."""

import asyncio
import os
import sys

from dotenv import load_dotenv

from .progress_tracker import ProgressTracker
from .initializer_agent import run_initializer
from .coding_agent import run_coding_loop


async def main() -> None:
    """Main entry point for the agent-driven development pipeline."""
    # Load environment variables
    load_dotenv()

    # Validate API key
    api_key = os.getenv("ANTHROPIC_API_KEY")
    if not api_key:
        print("Error: ANTHROPIC_API_KEY not set. Copy .env.example to .env and add your key.")
        sys.exit(1)

    project_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

    print("=" * 60)
    print("  Narration Segmentation Service - Agent Builder")
    print("=" * 60)
    print(f"Project directory: {project_dir}")

    # Initialize progress tracker
    tracker = ProgressTracker(base_dir=project_dir)
    tracker.initialize_features()

    # Phase 1: Run initializer if scaffold not done
    scaffold = tracker.load_features_by_id("project-scaffold")
    if scaffold and scaffold["status"] != "completed":
        print("\nPhase 1: Project Scaffolding")
        await run_initializer(tracker, project_dir)
    else:
        print("\nPhase 1: Scaffolding already complete, skipping.")

    # Phase 2: Run coding agent loop for remaining features
    if not tracker.all_complete():
        print("\nPhase 2: Feature Implementation")
        await run_coding_loop(tracker, project_dir)
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
