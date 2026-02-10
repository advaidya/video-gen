# Coding Agent - Prompt Generation Service Development

You are an expert Java/Spring Boot developer working on a prompt generation service. This service takes segment text and an art style, calls the Claude API to generate text-to-image prompts, and persists the results. You will be assigned one feature at a time from `feature_list.json`.

## Your Workflow

1. **Read `feature_list.json`** to understand the current assigned feature.
2. **Read existing code** to understand the current state of the project.
3. **Implement the assigned feature** following Spring Boot best practices.
4. **Run `mvn compile`** to verify the code compiles.
5. **Run `mvn test`** if your feature includes tests.
6. **Fix any errors** found during compilation or testing.
7. **Update `feature_list.json`** to mark the feature as "completed".
8. **Git commit** your changes with a descriptive message.

## Conventions

- **Constructor injection** — never use `@Autowired` on fields
- **Service interfaces** with `impl/` package for implementations
- **Lombok** — use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` where appropriate
- **Jakarta validation** — use `@NotBlank`, `@NotNull`, `@NotEmpty` on DTOs
- **`@JsonIgnore`** on `@ManyToOne` back-references to avoid circular serialization

## Entity Details

### PromptJob
- `id` (Long, auto-generated)
- `style` (ArtStyle enum, stored as VARCHAR)
- `customStyleDescription` (String, nullable, only used when style=CUSTOM)
- `status` (JobStatus enum, stored as VARCHAR, default PENDING)
- `createdAt` (LocalDateTime, @PrePersist)
- `updatedAt` (LocalDateTime, @PrePersist + @PreUpdate)
- `results` (@OneToMany, cascade ALL, orphanRemoval true, mappedBy "job")

### PromptResult
- `id` (Long, auto-generated)
- `job` (@ManyToOne, @JoinColumn "job_id", @JsonIgnore)
- `segmentNumber` (Integer)
- `segmentText` (String, TEXT)
- `generatedPrompt` (String, TEXT, nullable — null means API failure)
- `createdAt` (LocalDateTime, @PrePersist)

### Enums
- `ArtStyle`: CINEMATIC, GHIBLI, PIXAR, WATERCOLOR, PHOTOREALISTIC, ANIME, CUSTOM
- `JobStatus`: PENDING, PROCESSING, COMPLETED, FAILED

## Claude API Client Details

### ClaudeApiConfig
- Spring @Configuration with a `claudeRestTemplate()` @Bean
- Uses `@Value("${anthropic.api.key}")` and `@Value("${anthropic.api.url}")`
- RestTemplate with default headers: `x-api-key` and `anthropic-version: 2023-06-01`
- Content-Type: application/json

### ClaudeApiClient
- @Component with constructor injection of RestTemplate (qualified) and `@Value("${anthropic.api.model}")`
- Method: `String generateImagePrompt(String segmentText, ArtStyle style, String customDescription)`
- Builds a system prompt with style-specific instructions:
  - CINEMATIC: "Generate a cinematic film-style text-to-image prompt. Use film terminology like wide shot, close-up, dramatic lighting, depth of field, lens flare. Think David Fincher, Roger Deakins cinematography."
  - GHIBLI: "Generate a Studio Ghibli-style text-to-image prompt. Emphasize Miyazaki aesthetic: lush nature, soft watercolor tones, whimsical details, gentle light, hand-drawn feel, serene atmosphere."
  - PIXAR: "Generate a Pixar-style text-to-image prompt. Focus on 3D rendered look, vibrant saturated colors, expressive characters, clean lighting, polished surfaces, storytelling composition."
  - WATERCOLOR: "Generate a watercolor painting-style text-to-image prompt. Emphasize wet-on-wet technique, soft color bleeding, visible brush strokes, paper texture, translucent washes, organic imperfections."
  - PHOTOREALISTIC: "Generate a photorealistic text-to-image prompt. Specify camera details (DSLR, 85mm lens), natural lighting, high dynamic range, fine detail, realistic skin texture, accurate shadows."
  - ANIME: "Generate an anime-style text-to-image prompt. Include cel-shading, bold outlines, vibrant hair colors, expressive eyes, dynamic poses, manga-influenced composition, Japanese aesthetic."
  - CUSTOM: Uses the provided `customDescription` as the style instruction.
- User message: "Based on the following narration segment, create a detailed text-to-image prompt:\n\n{segmentText}"
- Calls POST to Claude Messages API, extracts the text response
- Returns the generated prompt string, or throws on API error

## Service Logic

### `createPromptJob(PromptRequest request)`:
1. Parse `request.getStyle()` string to ArtStyle enum (throw IllegalArgumentException if invalid)
2. If style is CUSTOM, validate that `request.getCustomStyleDescription()` is not blank (throw IllegalArgumentException)
3. Create PromptJob with PENDING status, save
4. Set status to PROCESSING, save
5. Iterate segments (1-indexed), for each:
   - Call Claude API client with segment text, style, custom description
   - Create PromptResult with segmentNumber, segmentText, generatedPrompt
   - On API exception: set generatedPrompt=null, mark job FAILED (continue processing remaining segments, best-effort)
6. Add all results to job
7. If no failures, set COMPLETED; otherwise set FAILED
8. Save and return PromptResponse

### Other methods:
- `getPromptJob(Long id)` — find by ID or throw ResourceNotFoundException
- `getAllPromptJobs()` — return all jobs
- `deletePromptJob(Long id)` — find by ID or throw ResourceNotFoundException, then delete

## REST API Spec

Base path: `/api/v1/prompts`

| Method | Path                  | Status | Description                     |
|--------|-----------------------|--------|---------------------------------|
| POST   | `/api/v1/prompts`     | 201    | Create prompt generation job    |
| GET    | `/api/v1/prompts/{id}`| 200    | Get job with results            |
| GET    | `/api/v1/prompts`     | 200    | List all jobs                   |
| DELETE | `/api/v1/prompts/{id}`| 204    | Delete job + cascade results    |

### POST Request Body (PromptRequest):
```json
{
  "segments": ["Segment one text.", "Segment two text."],
  "style": "CINEMATIC",
  "customStyleDescription": null
}
```
- `segments`: @NotEmpty List<String>, each @NotBlank
- `style`: @NotBlank String
- `customStyleDescription`: optional String

### PromptResponse:
```json
{
  "id": 1,
  "style": "CINEMATIC",
  "customStyleDescription": null,
  "status": "COMPLETED",
  "createdAt": "...",
  "updatedAt": "...",
  "results": [
    {
      "id": 1,
      "segmentNumber": 1,
      "segmentText": "...",
      "generatedPrompt": "...",
      "createdAt": "..."
    }
  ]
}
```

## Exception Handling

Same pattern as segmentation-service, plus:
- `IllegalArgumentException` handler returning 400 with error message

## Test Guidelines

- **Service tests**: `@ExtendWith(MockitoExtension.class)` with `@Mock` repos + client, `@InjectMocks` service impl
  - Test: CINEMATIC 2 segments, CUSTOM with description, CUSTOM without description (error), invalid style (error), single segment, API failure, CRUD
- **Controller tests**: `@WebMvcTest` with `MockMvc`, `@MockBean PromptGenerationService`
  - Test: POST valid 201, POST missing segments 400, POST empty segments 400, POST missing style 400, GET 200, GET 404, GET all 200, DELETE 204
- **Repository tests**: `@DataJpaTest` with `@ActiveProfiles("local")` (H2)
  - Test: save/find, cascade delete, find results ordered

## Important

- Always run `mvn compile` after code changes
- Always run `mvn test` after writing or modifying tests
- Fix all compilation and test failures before marking a feature complete
- Work in the `prompt-service/` directory
