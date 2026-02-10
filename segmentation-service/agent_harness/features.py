"""Feature definitions for the segmentation service."""

FEATURES = [
    {"id": "project-scaffold", "name": "Project Scaffolding", "status": "completed", "description": "Create directory structure, pom.xml, and skeleton classes"},
    {"id": "database-entities", "name": "Database Entities", "status": "completed", "description": "Implement NarrationScript and ScriptSegment JPA entities"},
    {"id": "repositories", "name": "Repositories", "status": "completed", "description": "Implement Spring Data JPA repositories"},
    {"id": "dtos", "name": "DTOs", "status": "completed", "description": "Implement ScriptRequest, ScriptResponse, SegmentResponse"},
    {"id": "segmentation-logic", "name": "Segmentation Logic", "status": "completed", "description": "Implement sentence-boundary segmentation algorithm (~20 words/segment)"},
    {"id": "rest-controller", "name": "REST Controller", "status": "completed", "description": "Implement CRUD endpoints at /api/v1/scripts"},
    {"id": "exception-handling", "name": "Exception Handling", "status": "completed", "description": "Implement GlobalExceptionHandler and ResourceNotFoundException"},
    {"id": "spring-profiles", "name": "Spring Profiles", "status": "completed", "description": "Configure local (H2), dev (MySQL), prod profiles"},
    {"id": "flyway-migration", "name": "Flyway Migration", "status": "completed", "description": "Set up Flyway with V1__init_schema.sql"},
    {"id": "unit-tests-service", "name": "Service Unit Tests", "status": "completed", "description": "Write SegmentationServiceTest with Mockito"},
    {"id": "unit-tests-controller", "name": "Controller Unit Tests", "status": "completed", "description": "Write ScriptControllerTest with MockMvc"},
    {"id": "unit-tests-repository", "name": "Repository Unit Tests", "status": "completed", "description": "Write NarrationScriptRepositoryTest with H2"},
    {"id": "docker-config", "name": "Docker Configuration", "status": "completed", "description": "Create Dockerfile and docker-compose.yml"},
    {"id": "actuator-health", "name": "Actuator Health", "status": "completed", "description": "Configure Spring Boot Actuator health endpoint"},
]
