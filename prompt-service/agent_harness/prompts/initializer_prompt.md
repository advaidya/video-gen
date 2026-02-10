# Initializer Agent - Prompt Service Scaffolding

You are an expert Java/Spring Boot developer. Your job is to scaffold a complete Spring Boot prompt generation service project.

## Your Tasks

1. **Create the full directory structure** under `prompt-service/`:
   ```
   prompt-service/
   ├── pom.xml
   ├── Dockerfile
   ├── docker-compose.yml
   └── src/
       ├── main/
       │   ├── java/com/videogen/promptgen/
       │   │   ├── PromptServiceApplication.java
       │   │   ├── config/
       │   │   │   ├── AppConfig.java
       │   │   │   └── ClaudeApiConfig.java
       │   │   ├── controller/PromptController.java
       │   │   ├── dto/
       │   │   │   ├── PromptRequest.java
       │   │   │   ├── PromptResponse.java
       │   │   │   └── PromptResultResponse.java
       │   │   ├── exception/
       │   │   │   ├── GlobalExceptionHandler.java
       │   │   │   └── ResourceNotFoundException.java
       │   │   ├── model/
       │   │   │   ├── ArtStyle.java
       │   │   │   ├── JobStatus.java
       │   │   │   ├── PromptJob.java
       │   │   │   └── PromptResult.java
       │   │   ├── repository/
       │   │   │   ├── PromptJobRepository.java
       │   │   │   └── PromptResultRepository.java
       │   │   ├── client/ClaudeApiClient.java
       │   │   └── service/
       │   │       ├── PromptGenerationService.java
       │   │       └── impl/PromptGenerationServiceImpl.java
       │   └── resources/
       │       ├── application.yml
       │       ├── application-local.yml
       │       ├── application-dev.yml
       │       ├── application-prod.yml
       │       └── db/migration/V1__init_schema.sql
       └── test/java/com/videogen/promptgen/
           ├── service/PromptGenerationServiceTest.java
           ├── controller/PromptControllerTest.java
           └── repository/PromptJobRepositoryTest.java
   ```

2. **Generate `pom.xml`** with:
   - Parent: `spring-boot-starter-parent:3.2.5`
   - Java 17
   - groupId: `com.videogen`, artifactId: `prompt-service`
   - Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-actuator, spring-boot-starter-validation, mysql-connector-j, flyway-core, flyway-mysql, lombok (1.18.42), h2 (runtime scope), spring-boot-starter-test
   - maven-compiler-plugin with Lombok annotation processor path

3. **Create enums:**
   - `ArtStyle`: CINEMATIC, GHIBLI, PIXAR, WATERCOLOR, PHOTOREALISTIC, ANIME, CUSTOM
   - `JobStatus`: PENDING, PROCESSING, COMPLETED, FAILED

4. **Set up `application.yml`** with base config:
   - server.port=8081
   - actuator health endpoint enabled
   - Spring profiles: local, dev, prod
   - anthropic.api.key, anthropic.api.url, anthropic.api.model properties

5. **Create Flyway migration** `V1__init_schema.sql` with:
   ```sql
   CREATE TABLE prompt_jobs (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       style VARCHAR(50) NOT NULL,
       custom_style_description TEXT,
       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );

   CREATE TABLE prompt_results (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       job_id BIGINT NOT NULL,
       segment_number INT NOT NULL,
       segment_text TEXT NOT NULL,
       generated_prompt TEXT,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       CONSTRAINT fk_prompt_results_job
           FOREIGN KEY (job_id) REFERENCES prompt_jobs(id) ON DELETE CASCADE
   );

   CREATE INDEX idx_prompt_results_job_id ON prompt_results(job_id);
   ```

6. **Create Docker files**:
   - Multi-stage Dockerfile (temurin:17-jdk build, temurin:17-jre run)
   - docker-compose.yml with MySQL 8.0 service (port 3307 to avoid conflict) + app service (port 8081)

## Important Notes
- Use constructor injection (not field injection) in all Spring components
- Use Lombok @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor on entities and DTOs
- Follow Spring Boot 3.x / Jakarta EE conventions
- All skeleton files should compile without errors
