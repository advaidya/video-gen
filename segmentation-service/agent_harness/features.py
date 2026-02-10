"""Feature definitions for the segmentation service."""

FEATURES = [
    {"id": "project-scaffold", "name": "Project Scaffolding", "status": "pending", "description": "Create directory structure, pom.xml, and skeleton classes"},
    {"id": "database-entities", "name": "Database Entities", "status": "pending", "description": "Implement NarrationScript and ScriptSegment JPA entities"},
    {"id": "repositories", "name": "Repositories", "status": "pending", "description": "Implement Spring Data JPA repositories"},
    {"id": "dtos", "name": "DTOs", "status": "pending", "description": "Implement ScriptRequest, ScriptResponse, SegmentResponse"},
    {"id": "segmentation-logic", "name": "Segmentation Logic", "status": "pending", "description": "Implement sentence-boundary segmentation algorithm (~20 words/segment)"},
    {"id": "rest-controller", "name": "REST Controller", "status": "pending", "description": "Implement CRUD endpoints at /api/v1/scripts"},
    {"id": "exception-handling", "name": "Exception Handling", "status": "pending", "description": "Implement GlobalExceptionHandler and ResourceNotFoundException"},
    {"id": "spring-profiles", "name": "Spring Profiles", "status": "pending", "description": "Configure local (H2), dev (MySQL), prod profiles"},
    {"id": "flyway-migration", "name": "Flyway Migration", "status": "pending", "description": "Set up Flyway with V1__init_schema.sql"},
    {"id": "unit-tests-service", "name": "Service Unit Tests", "status": "pending", "description": "Write SegmentationServiceTest with Mockito"},
    {"id": "unit-tests-controller", "name": "Controller Unit Tests", "status": "pending", "description": "Write ScriptControllerTest with MockMvc"},
    {"id": "unit-tests-repository", "name": "Repository Unit Tests", "status": "pending", "description": "Write NarrationScriptRepositoryTest with H2"},
    {"id": "docker-config", "name": "Docker Configuration", "status": "pending", "description": "Create Dockerfile and docker-compose.yml"},
    {"id": "actuator-health", "name": "Actuator Health", "status": "pending", "description": "Configure Spring Boot Actuator health endpoint"},
]
