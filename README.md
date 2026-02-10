# Video Generation Pipeline

A multi-service project demonstrating agent-driven development: **Python agent harnesses** (using the Claude Agent SDK) orchestrate autonomous coding of **Spring Boot REST services**.

## Architecture

```
video_gen/
├── agent_common/              # Shared Python library
│   ├── security.py            # Bash command allowlist
│   ├── progress_tracker.py    # Parameterized feature tracking
│   └── runner.py              # Generic initializer + coding loop
├── segmentation-service/      # Narration segmentation (port 8080)
│   ├── agent_harness/         # Per-service agent harness
│   │   ├── features.py        # 14 segmentation features
│   │   ├── main.py            # Entry point
│   │   └── prompts/           # Initializer + coding prompts
│   └── src/...                # Spring Boot service
├── prompt-service/            # Prompt generation (port 8081)
│   ├── agent_harness/         # Per-service agent harness
│   │   ├── features.py        # 12 prompt features
│   │   ├── main.py            # Entry point
│   │   └── prompts/           # Initializer + coding prompts
│   └── src/...                # Spring Boot service
```

Each service has its own agent harness that imports shared logic from `agent_common/`.

## Services

### Segmentation Service (port 8080)
Segments narration scripts into ~8-second audio segments.

- **Base URL**: `http://localhost:8080/api/v1/scripts`
- **Endpoints**: POST (create + segment), GET (by ID), GET (list all), PUT (update + re-segment), DELETE

### Prompt Generation Service (port 8081)
Takes segment text + art style, calls the Claude API to generate text-to-image prompts.

- **Base URL**: `http://localhost:8081/api/v1/prompts`
- **Endpoints**: POST (create job), GET (by ID), GET (list all), DELETE
- **Art Styles**: CINEMATIC, GHIBLI, PIXAR, WATERCOLOR, PHOTOREALISTIC, ANIME, CUSTOM

## Prerequisites

- Python 3.10+
- Java 17+
- Maven 3.8+
- MySQL 8.0+ (or Docker)
- Claude API key (for agent harness and prompt-service)
- Node.js 18+ (for Claude Agent SDK)

## Quick Start

```bash
# 1. Set up environment
cp .env.example .env
# Edit .env with your ANTHROPIC_API_KEY

# 2. Run Segmentation Service (local profile with H2)
cd segmentation-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. Run Prompt Service (local profile with H2, in a separate terminal)
cd prompt-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Test the APIs

```bash
# Segmentation Service
curl -X POST http://localhost:8080/api/v1/scripts \
  -H "Content-Type: application/json" \
  -d '{"title":"Demo","rawText":"This is a test narration. It has multiple sentences. Each one should be grouped into segments."}'

# Prompt Service
curl -X POST http://localhost:8081/api/v1/prompts \
  -H "Content-Type: application/json" \
  -d '{"segments":["A hero walks through a misty forest at dawn.","The castle looms in the distance."],"style":"CINEMATIC"}'
```

## Agent Usage

Each service has its own agent harness:

```bash
# Run segmentation-service agent
python -m segmentation-service.agent_harness.main

# Run prompt-service agent
python -m prompt-service.agent_harness.main
```

Each agent:
1. Runs the **initializer agent** to scaffold the Spring Boot project
2. Runs the **coding agent loop** to implement features iteratively

## Manual Service Usage

### Build and Run

```bash
# Segmentation Service
cd segmentation-service
mvn clean compile && mvn test
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Prompt Service
cd prompt-service
mvn clean compile && mvn test
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Docker

```bash
# Segmentation Service (MySQL on port 3306)
cd segmentation-service && docker-compose up --build

# Prompt Service (MySQL on port 3307)
cd prompt-service && docker-compose up --build
```

## Segmentation Service API

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/api/v1/scripts` | 201 | Create and segment a script |
| GET | `/api/v1/scripts/{id}` | 200 | Get script with segments |
| GET | `/api/v1/scripts` | 200 | List all scripts |
| PUT | `/api/v1/scripts/{id}` | 200 | Update and re-segment |
| DELETE | `/api/v1/scripts/{id}` | 204 | Delete script and segments |

## Prompt Service API

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/api/v1/prompts` | 201 | Create prompt generation job |
| GET | `/api/v1/prompts/{id}` | 200 | Get job with results |
| GET | `/api/v1/prompts` | 200 | List all jobs |
| DELETE | `/api/v1/prompts/{id}` | 204 | Delete job + results |

### Prompt Request Example

```json
{
  "segments": ["A hero walks through a misty forest at dawn.", "The castle looms in the distance."],
  "style": "CINEMATIC",
  "customStyleDescription": null
}
```

### Prompt Response Example

```json
{
  "id": 1,
  "style": "CINEMATIC",
  "customStyleDescription": null,
  "status": "COMPLETED",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00",
  "results": [
    {
      "id": 1,
      "segmentNumber": 1,
      "segmentText": "A hero walks through a misty forest at dawn.",
      "generatedPrompt": "A cinematic wide shot of a lone figure walking...",
      "createdAt": "2025-01-15T10:30:01"
    }
  ]
}
```

## Spring Profiles

Both services support three profiles:

- `local` - H2 in-memory database, Flyway disabled
- `dev` - MySQL via environment variables, Flyway enabled
- `prod` - MySQL with HikariCP connection pooling, minimal logging

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `ANTHROPIC_API_KEY` | Claude API key (agent harness + prompt-service) | - |
| `MYSQL_HOST` | MySQL hostname | `localhost` |
| `MYSQL_PORT` | MySQL port | `3306` (seg) / `3307` (prompt) |
| `MYSQL_DATABASE` | Database name | `segmentation_db` / `prompt_db` |
| `MYSQL_USER` | Database user | `root` |
| `MYSQL_PASSWORD` | Database password | - |

## Health Checks

```bash
curl http://localhost:8080/actuator/health  # Segmentation Service
curl http://localhost:8081/actuator/health  # Prompt Service
```

## Troubleshooting

### Maven build fails
- Ensure Java 17+ is installed: `java -version`
- Ensure Maven 3.8+ is installed: `mvn -version`

### Tests fail with database errors
- Use the `local` profile: `mvn test -Dspring.profiles.active=local`

### Port conflict
- Segmentation runs on 8080, Prompt on 8081
- MySQL: segmentation on 3306, prompt on 3307
