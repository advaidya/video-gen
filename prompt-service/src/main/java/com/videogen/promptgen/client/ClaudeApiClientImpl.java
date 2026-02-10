package com.videogen.promptgen.client;

import com.videogen.promptgen.model.ArtStyle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class ClaudeApiClientImpl implements ClaudeApiClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String model;

    public ClaudeApiClientImpl(
            @Qualifier("claudeRestTemplate") RestTemplate restTemplate,
            @Value("${anthropic.api.url}") String apiUrl,
            @Value("${anthropic.api.model}") String model) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    @Override
    public String generateImagePrompt(String segmentText, ArtStyle style, String customDescription) {
        String systemPrompt = buildSystemPrompt(style, customDescription);
        String userMessage = "Based on the following narration segment, create a detailed text-to-image prompt:\n\n" + segmentText;

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", userMessage)
                )
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(apiUrl, requestBody, Map.class);

        if (response == null) {
            throw new RuntimeException("Claude API returned null response");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("Claude API returned empty content");
        }

        return (String) content.get(0).get("text");
    }

    private String buildSystemPrompt(ArtStyle style, String customDescription) {
        return switch (style) {
            case CINEMATIC -> "Generate a cinematic film-style text-to-image prompt. Use film terminology like wide shot, close-up, dramatic lighting, depth of field, lens flare. Think David Fincher, Roger Deakins cinematography.";
            case GHIBLI -> "Generate a Studio Ghibli-style text-to-image prompt. Emphasize Miyazaki aesthetic: lush nature, soft watercolor tones, whimsical details, gentle light, hand-drawn feel, serene atmosphere.";
            case PIXAR -> "Generate a Pixar-style text-to-image prompt. Focus on 3D rendered look, vibrant saturated colors, expressive characters, clean lighting, polished surfaces, storytelling composition.";
            case WATERCOLOR -> "Generate a watercolor painting-style text-to-image prompt. Emphasize wet-on-wet technique, soft color bleeding, visible brush strokes, paper texture, translucent washes, organic imperfections.";
            case PHOTOREALISTIC -> "Generate a photorealistic text-to-image prompt. Specify camera details (DSLR, 85mm lens), natural lighting, high dynamic range, fine detail, realistic skin texture, accurate shadows.";
            case ANIME -> "Generate an anime-style text-to-image prompt. Include cel-shading, bold outlines, vibrant hair colors, expressive eyes, dynamic poses, manga-influenced composition, Japanese aesthetic.";
            case CUSTOM -> customDescription != null ? customDescription : "Generate a detailed text-to-image prompt.";
        };
    }
}
