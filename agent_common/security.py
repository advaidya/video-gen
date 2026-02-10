"""Security filters for agent bash command execution."""

from claude_agent_sdk.types import PermissionResultAllow, PermissionResultDeny

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


async def bash_security_filter(tool_name, tool_input, context):
    """Security callback for ClaudeAgentOptions.can_use_tool.

    Allows all non-Bash tools freely. For Bash tools, validates the command
    against the allowlist and blocked patterns.
    """
    if tool_name != "Bash":
        return PermissionResultAllow()

    command = tool_input.get("command", "")

    # Check blocked patterns
    for pattern in BLOCKED_PATTERNS:
        if pattern in command:
            print(f"[SECURITY] Blocked command matching pattern '{pattern}': {command}")
            return PermissionResultDeny(message=f"Blocked pattern: {pattern}")

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
        return PermissionResultDeny(message="Could not parse command")

    if base_command not in ALLOWED_BASH_COMMANDS:
        print(f"[SECURITY] Command '{base_command}' not in allowlist: {command}")
        return PermissionResultDeny(message=f"Command '{base_command}' not in allowlist")

    return PermissionResultAllow()
