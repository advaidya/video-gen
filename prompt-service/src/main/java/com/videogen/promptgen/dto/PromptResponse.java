package com.videogen.promptgen.dto;

import com.videogen.promptgen.model.PromptJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {

    private Long id;
    private String style;
    private String customStyleDescription;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PromptResultResponse> results;

    public static PromptResponse fromEntity(PromptJob job) {
        List<PromptResultResponse> resultResponses = job.getResults().stream()
                .map(PromptResultResponse::fromEntity)
                .collect(Collectors.toList());

        return PromptResponse.builder()
                .id(job.getId())
                .style(job.getStyle().name())
                .customStyleDescription(job.getCustomStyleDescription())
                .status(job.getStatus().name())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .results(resultResponses)
                .build();
    }
}
