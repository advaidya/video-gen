package com.videogen.segmentation.dto;

import com.videogen.segmentation.model.ScriptSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentResponse {

    private Long id;
    private Integer segmentNumber;
    private String segmentText;
    private Double estimatedDurationSeconds;
    private Integer wordCount;

    public static SegmentResponse fromEntity(ScriptSegment segment) {
        return SegmentResponse.builder()
                .id(segment.getId())
                .segmentNumber(segment.getSegmentNumber())
                .segmentText(segment.getSegmentText())
                .estimatedDurationSeconds(segment.getEstimatedDurationSeconds())
                .wordCount(segment.getWordCount())
                .build();
    }
}
