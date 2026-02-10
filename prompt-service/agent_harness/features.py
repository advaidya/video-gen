"""Feature definitions for the prompt generation service."""

FEATURES = [
    {"id": "project-scaffold", "name": "Project Scaffolding", "status": "pending", "description": "Create directory structure, pom.xml, and skeleton classes for prompt-service"},
    {"id": "database-entities", "name": "Database Entities", "status": "pending", "description": "Implement PromptJob and PromptResult JPA entities with ArtStyle and JobStatus enums"},
    {"id": "repositories", "name": "Repositories", "status": "pending", "description": "Implement PromptJobRepository and PromptResultRepository"},
    {"id": "dtos", "name": "DTOs", "status": "pending", "description": "Implement PromptRequest, PromptResponse, PromptResultResponse"},
    {"id": "claude-api-client", "name": "Claude API Client", "status": "pending", "description": "Implement ClaudeApiConfig and ClaudeApiClient for calling Claude Messages API"},
    {"id": "prompt-generation-logic", "name": "Prompt Generation Logic", "status": "pending", "description": "Implement PromptGenerationService with style-specific prompt generation"},
    {"id": "rest-controller", "name": "REST Controller", "status": "pending", "description": "Implement CRUD endpoints at /api/v1/prompts"},
    {"id": "exception-handling", "name": "Exception Handling", "status": "pending", "description": "Implement GlobalExceptionHandler, ResourceNotFoundException, IllegalArgumentException handler"},
    {"id": "spring-profiles", "name": "Spring Profiles", "status": "pending", "description": "Configure local (H2, port 8081), dev (MySQL), prod profiles"},
    {"id": "unit-tests-service", "name": "Service Unit Tests", "status": "pending", "description": "Write PromptGenerationServiceTest with Mockito"},
    {"id": "unit-tests-controller", "name": "Controller Unit Tests", "status": "pending", "description": "Write PromptControllerTest with MockMvc"},
    {"id": "unit-tests-repository", "name": "Repository Unit Tests", "status": "pending", "description": "Write PromptJobRepositoryTest with H2"},
]
