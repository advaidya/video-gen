"""Feature definitions for the prompt generation service."""

FEATURES = [
    {"id": "project-scaffold", "name": "Project Scaffolding", "status": "completed", "description": "Create directory structure, pom.xml, and skeleton classes for prompt-service"},
    {"id": "database-entities", "name": "Database Entities", "status": "completed", "description": "Implement PromptJob and PromptResult JPA entities with ArtStyle and JobStatus enums"},
    {"id": "repositories", "name": "Repositories", "status": "completed", "description": "Implement PromptJobRepository and PromptResultRepository"},
    {"id": "dtos", "name": "DTOs", "status": "completed", "description": "Implement PromptRequest, PromptResponse, PromptResultResponse"},
    {"id": "claude-api-client", "name": "Claude API Client", "status": "completed", "description": "Implement ClaudeApiConfig and ClaudeApiClient for calling Claude Messages API"},
    {"id": "prompt-generation-logic", "name": "Prompt Generation Logic", "status": "completed", "description": "Implement PromptGenerationService with style-specific prompt generation"},
    {"id": "rest-controller", "name": "REST Controller", "status": "completed", "description": "Implement CRUD endpoints at /api/v1/prompts"},
    {"id": "exception-handling", "name": "Exception Handling", "status": "completed", "description": "Implement GlobalExceptionHandler, ResourceNotFoundException, IllegalArgumentException handler"},
    {"id": "spring-profiles", "name": "Spring Profiles", "status": "completed", "description": "Configure local (H2, port 8081), dev (MySQL), prod profiles"},
    {"id": "unit-tests-service", "name": "Service Unit Tests", "status": "completed", "description": "Write PromptGenerationServiceTest with Mockito"},
    {"id": "unit-tests-controller", "name": "Controller Unit Tests", "status": "completed", "description": "Write PromptControllerTest with MockMvc"},
    {"id": "unit-tests-repository", "name": "Repository Unit Tests", "status": "completed", "description": "Write PromptJobRepositoryTest with H2"},
]
