"""Security filters for agent bash command execution."""

ALLOWED_BASH_COMMANDS = {
    "mvn", "java", "git", "mkdir", "ls", "cat", "cp", "mv",
    "curl", "find", "grep", "chmod", "echo", "touch", "pwd",
    "tree", "docker", "docker-compose",
}

BLOCKED_PATTERNS = [
    "rm -rf /",
    "sudo",
    "> /etc",
    "curl | sh",
]


async def bash_security_filter(tool_name: str, tool_input: dict) -> bool:
    """Security callback for ClaudeAgentOptions.can_use_tool.

    Allows all non-Bash tools freely. For Bash tools, validates the command
    against the allowlist and blocked patterns.
    """
    if tool_name != "Bash":
        return True

    command = tool_input.get("command", "")

    # Check blocked patterns
    for pattern in BLOCKED_PATTERNS:
        if pattern in command:
            print(f"[SECURITY] Blocked command matching pattern '{pattern}': {command}")
            return False

    # Extract the base command (first word, ignoring env vars)
    parts = command.strip().split()
    base_command = None
    for part in parts:
        if "=" not in part:
            # Strip any path prefix to get the command name
            base_command = part.split("/")[-1]
            break

    if base_command is None:
        print(f"[SECURITY] Could not parse command: {command}")
        return False

    if base_command not in ALLOWED_BASH_COMMANDS:
        print(f"[SECURITY] Command '{base_command}' not in allowlist: {command}")
        return False

    return True
