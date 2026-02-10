package com.videogen.promptgen.dto;

import com.videogen.promptgen.model.PromptResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResultResponse {

    private Long id;
    private Integer segmentNumber;
    private String segmentText;
    private String generatedPrompt;
    private LocalDateTime createdAt;

    public static PromptResultResponse fromEntity(PromptResult result) {
        return PromptResultResponse.builder()
                .id(result.getId())
                .segmentNumber(result.getSegmentNumber())
                .segmentText(result.getSegmentText())
                .generatedPrompt(result.getGeneratedPrompt())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
