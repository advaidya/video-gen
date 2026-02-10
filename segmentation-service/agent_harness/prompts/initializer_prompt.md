# Initializer Agent - Project Scaffolding

You are an expert Java/Spring Boot developer. Your job is to scaffold a complete Spring Boot narration segmentation service project.

## Your Tasks

1. **Create the full directory structure** under `segmentation-service/`:
   ```
   segmentation-service/
   ├── pom.xml
   ├── Dockerfile
   ├── docker-compose.yml
   └── src/
       ├── main/
       │   ├── java/com/videogen/segmentation/
       │   │   ├── SegmentationServiceApplication.java
       │   │   ├── config/AppConfig.java
       │   │   ├── controller/ScriptController.java
       │   │   ├── dto/
       │   │   │   ├── ScriptRequest.java
       │   │   │   ├── ScriptResponse.java
       │   │   │   └── SegmentResponse.java
       │   │   ├── exception/
       │   │   │   ├── GlobalExceptionHandler.java
       │   │   │   └── ResourceNotFoundException.java
       │   │   ├── model/
       │   │   │   ├── NarrationScript.java
       │   │   │   └── ScriptSegment.java
       │   │   ├── repository/
       │   │   │   ├── NarrationScriptRepository.java
       │   │   │   └── ScriptSegmentRepository.java
       │   │   └── service/
       │   │       ├── SegmentationService.java
       │   │       └── impl/SegmentationServiceImpl.java
       │   └── resources/
       │       ├── application.yml
       │       ├── application-local.yml
       │       ├── application-dev.yml
       │       ├── application-prod.yml
       │       └── db/migration/V1__init_schema.sql
       └── test/java/com/videogen/segmentation/
           ├── service/SegmentationServiceTest.java
           ├── controller/ScriptControllerTest.java
           └── repository/NarrationScriptRepositoryTest.java
   ```

2. **Generate `pom.xml`** with:
   - Parent: `spring-boot-starter-parent:3.2.5`
   - Java 17
   - Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-actuator, spring-boot-starter-validation, mysql-connector-j, flyway-core, flyway-mysql, lombok, h2 (test scope), spring-boot-starter-test

3. **Create skeleton classes** for all packages with correct package declarations, basic annotations, and TODO comments where implementation is needed.

4. **Set up `application.yml`** with base config:
   - server.port=8080
   - actuator health endpoint enabled
   - Spring profiles: local, dev, prod

5. **Create Flyway migration** `V1__init_schema.sql` with:
   ```sql
   CREATE TABLE narration_scripts (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       title VARCHAR(255) NOT NULL,
       raw_text TEXT NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );

   CREATE TABLE script_segments (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       script_id BIGINT NOT NULL,
       segment_number INT NOT NULL,
       segment_text TEXT NOT NULL,
       estimated_duration_seconds DOUBLE NOT NULL,
       word_count INT NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       CONSTRAINT fk_script_segments_script
           FOREIGN KEY (script_id) REFERENCES narration_scripts(id) ON DELETE CASCADE
   );

   CREATE INDEX idx_segments_script_id ON script_segments(script_id);
   ```

6. **Create Docker files**:
   - Multi-stage Dockerfile (temurin:17-jdk build, temurin:17-jre run)
   - docker-compose.yml with MySQL 8.0 service + app service

7. **Initialize a git repo** in the project root and make an initial commit.

## Important Notes
- Use constructor injection (not field injection) in all Spring components
- Use Lombok @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor on entities and DTOs
- Follow Spring Boot 3.x / Jakarta EE conventions
- All skeleton files should compile without errors
- Update the feature_list.json to mark "project-scaffold" as "completed" when done
