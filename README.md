# Narration Segmentation Service

A two-component project demonstrating agent-driven development: a **Python agent harness** (using the Claude Agent SDK) that orchestrates autonomous coding, and a **Spring Boot REST service** that segments narration scripts into ~8-second audio segments.

## Architecture

```
                    ┌─────────────────────┐
                    │   Agent Harness     │
                    │   (Python CLI)      │
                    └────────┬────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼                              ▼
   ┌──────────────────┐          ┌──────────────────┐
   │ Initializer Agent │          │  Coding Agent    │
   │ (One-shot)        │          │  (Iterative)     │
   └────────┬─────────┘          └────────┬─────────┘
            │                              │
            ▼                              ▼
   ┌──────────────────┐          ┌──────────────────┐
   │ Project Scaffold  │ ──────▶ │ Feature-by-      │
   │ (dirs, pom.xml,   │         │ Feature Dev      │
   │  skeletons)       │         │ (14 features)    │
   └──────────────────┘          └────────┬─────────┘
                                          │
                                          ▼
                                ┌──────────────────┐
                                │ Segmentation      │
                                │ Service           │
                                │ (Spring Boot)     │
                                └──────────────────┘
```

## Prerequisites

- Python 3.10+
- Java 17+
- Maven 3.8+
- MySQL 8.0+ (or Docker)
- Claude API key (for agent harness)
- Node.js 18+ (for Claude Agent SDK)

## Quick Start

```bash
# 1. Clone and set up environment
cp .env.example .env
# Edit .env with your ANTHROPIC_API_KEY

# 2. Install Python dependencies
pip install -r requirements.txt

# 3. Run the service directly (local profile with H2)
cd segmentation-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. Test the API
curl -X POST http://localhost:8080/api/v1/scripts \
  -H "Content-Type: application/json" \
  -d '{"title":"Demo","rawText":"This is a test narration. It has multiple sentences. Each one should be grouped into segments of about eight seconds duration."}'
```

## Agent Usage

### Run the Full Pipeline

```bash
python -m agent_harness.main
```

This will:
1. Run the **initializer agent** to scaffold the Spring Boot project (if not already done)
2. Run the **coding agent loop** to implement all 14 features iteratively

### Run Initializer Only

The initializer agent scaffolds the project structure, `pom.xml`, skeleton classes, and Docker files.

### Run Coding Agent Only

The coding agent picks up where the initializer left off, implementing features tracked in `feature_list.json`.

## Manual Service Usage

### Build and Run

```bash
cd segmentation-service

# Compile
mvn clean compile

# Run tests
mvn test

# Run with H2 (local profile)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run with MySQL (dev profile)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

```bash
cd segmentation-service
docker-compose up --build
```

## API Documentation

Base URL: `http://localhost:8080/api/v1/scripts`

### Endpoints

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| POST | `/api/v1/scripts` | 201 | Create and segment a script |
| GET | `/api/v1/scripts/{id}` | 200 | Get script with segments |
| GET | `/api/v1/scripts` | 200 | List all scripts |
| PUT | `/api/v1/scripts/{id}` | 200 | Update and re-segment |
| DELETE | `/api/v1/scripts/{id}` | 204 | Delete script and segments |

### Example Request

```bash
curl -X POST http://localhost:8080/api/v1/scripts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Product Demo",
    "rawText": "Welcome to our product demonstration. Today we will walk through the key features of our platform. Each feature has been designed with the user in mind. Let us start with the dashboard overview. The dashboard provides real-time analytics and reporting. You can customize the layout to match your workflow."
  }'
```

### Example Response

```json
{
  "id": 1,
  "title": "Product Demo",
  "rawText": "Welcome to our product demonstration...",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00",
  "segments": [
    {
      "id": 1,
      "segmentNumber": 1,
      "segmentText": "Welcome to our product demonstration. Today we will walk through the key features of our platform. Each feature has been designed with the user in mind.",
      "estimatedDurationSeconds": 10.8,
      "wordCount": 27
    },
    {
      "id": 2,
      "segmentNumber": 2,
      "segmentText": "Let us start with the dashboard overview. The dashboard provides real-time analytics and reporting. You can customize the layout to match your workflow.",
      "estimatedDurationSeconds": 9.6,
      "wordCount": 24
    }
  ]
}
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Testing from the Browser

Since browsers can only make GET requests from the address bar, you can directly test these endpoints:

- **List all scripts**: http://localhost:8080/api/v1/scripts
- **Get a specific script**: http://localhost:8080/api/v1/scripts/1 (after creating one)
- **Health check**: http://localhost:8080/actuator/health

For POST, PUT, and DELETE requests, use one of these browser-based options:

**Option 1 — Browser DevTools (no extensions needed)**

Open any page on `localhost:8080`, press `F12` to open DevTools, go to the **Console** tab, and paste:

```javascript
// Create a script
fetch('/api/v1/scripts', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    title: 'Browser Test',
    rawText: 'This is a test narration from the browser. It has multiple sentences. Each one should be grouped into segments of about eight seconds duration.'
  })
}).then(r => r.json()).then(console.log);

// Update a script (change the ID as needed)
fetch('/api/v1/scripts/1', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    title: 'Updated Title',
    rawText: 'New text content here. With multiple sentences for segmentation.'
  })
}).then(r => r.json()).then(console.log);

// Delete a script
fetch('/api/v1/scripts/1', { method: 'DELETE' }).then(r => console.log('Status:', r.status));
```

**Option 2 — REST Client extensions**

Use a browser extension like [Talend API Tester](https://chromewebstore.google.com/detail/talend-api-tester-free-ed/aejoelaoggembcahagimdiliamlcdmfm) (Chrome) or [RESTClient](https://addons.mozilla.org/en-US/firefox/addon/restclient/) (Firefox) to send requests with any HTTP method and JSON body.

### Inspecting the H2 Database

When running with the `local` profile, the service uses an H2 in-memory database with a built-in web console.

1. Start the service: `mvn spring-boot:run -Dspring-boot.run.profiles=local`
2. Open the H2 console in your browser: http://localhost:8080/h2-console
3. Enter the connection details:
   - **JDBC URL**: `jdbc:h2:mem:segmentation_db`
   - **User Name**: `sa`
   - **Password**: *(leave blank)*
4. Click **Connect**

You can then run SQL queries directly:

```sql
-- View all scripts
SELECT * FROM NARRATION_SCRIPTS;

-- View all segments
SELECT * FROM SCRIPT_SEGMENTS;

-- View segments for a specific script
SELECT s.title, seg.segment_number, seg.segment_text, seg.word_count, seg.estimated_duration_seconds
FROM NARRATION_SCRIPTS s
JOIN SCRIPT_SEGMENTS seg ON s.id = seg.script_id
WHERE s.id = 1
ORDER BY seg.segment_number;

-- Count segments per script
SELECT s.id, s.title, COUNT(seg.id) AS segment_count
FROM NARRATION_SCRIPTS s
LEFT JOIN SCRIPT_SEGMENTS seg ON s.id = seg.script_id
GROUP BY s.id, s.title;
```

> **Note**: The H2 database is in-memory, so all data is lost when the service stops. This is by design for local development.

## How the Agents Work

### Feature Tracking

The system tracks 14 features in `feature_list.json`:

1. `project-scaffold` - Directory structure and build config
2. `database-entities` - JPA entities
3. `repositories` - Spring Data repositories
4. `dtos` - Data transfer objects
5. `segmentation-logic` - Text segmentation algorithm
6. `rest-controller` - REST API endpoints
7. `exception-handling` - Error handling
8. `spring-profiles` - Environment configs
9. `flyway-migration` - Database migrations
10. `unit-tests-service` - Service layer tests
11. `unit-tests-controller` - Controller tests
12. `unit-tests-repository` - Repository tests
13. `docker-config` - Containerization
14. `actuator-health` - Health monitoring

### Progress Tracking

- `feature_list.json` - Machine-readable progress (JSON)
- `claude-progress.txt` - Human-readable progress log

### Session Management

- The initializer agent runs as a one-shot session
- The coding agent runs one session per feature with up to 30 turns
- Failed features are retried up to 3 times

### Security

Bash commands are filtered through an allowlist. Only safe commands (mvn, java, git, mkdir, etc.) are permitted. Dangerous patterns (rm -rf /, sudo, etc.) are blocked.

## Segmentation Algorithm

The algorithm splits narration text into segments targeting ~8 seconds of spoken audio:

- **Speaking rate**: 2.5 words/second
- **Target duration**: 8.0 seconds
- **Target words**: 20 words per segment
- Splits at sentence boundaries (`.` `!` `?`)
- Single long sentences stay whole
- Last segment may be shorter

## Cloud Deployment

### Spring Profiles

- `local` - H2 in-memory database, Flyway disabled
- `dev` - MySQL via environment variables, Flyway enabled
- `prod` - MySQL with HikariCP connection pooling, minimal logging

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `MYSQL_HOST` | MySQL hostname | `localhost` |
| `MYSQL_PORT` | MySQL port | `3306` |
| `MYSQL_DATABASE` | Database name | `segmentation_db` |
| `MYSQL_USER` | Database user | `root` |
| `MYSQL_PASSWORD` | Database password | - |

## Troubleshooting

### Maven build fails
- Ensure Java 17+ is installed: `java -version`
- Ensure Maven 3.8+ is installed: `mvn -version`

### Tests fail with database errors
- Make sure you're using the `local` profile for testing (H2 in-memory)
- Run: `mvn test -Dspring.profiles.active=local`

### Agent harness import errors
- Install dependencies: `pip install -r requirements.txt`
- Ensure Python 3.10+: `python --version`

### MySQL connection refused
- Start MySQL or use Docker: `docker-compose up mysql`
- Check env vars in `.env` match your MySQL config

### Port 8080 already in use
- Change the port: `mvn spring-boot:run -Dserver.port=8081`
