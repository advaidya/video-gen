package com.videogen.segmentation.dto;

import com.videogen.segmentation.model.NarrationScript;
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
public class ScriptResponse {

    private Long id;
    private String title;
    private String rawText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SegmentResponse> segments;

    public static ScriptResponse fromEntity(NarrationScript script) {
        List<SegmentResponse> segmentResponses = script.getSegments().stream()
                .map(SegmentResponse::fromEntity)
                .collect(Collectors.toList());

        return ScriptResponse.builder()
                .id(script.getId())
                .title(script.getTitle())
                .rawText(script.getRawText())
                .createdAt(script.getCreatedAt())
                .updatedAt(script.getUpdatedAt())
                .segments(segmentResponses)
                .build();
    }
}
