package com.videogen.promptgen.client;

import com.videogen.promptgen.model.ArtStyle;

public interface ClaudeApiClient {

    String generateImagePrompt(String segmentText, ArtStyle style, String customDescription);
}
