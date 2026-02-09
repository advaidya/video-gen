package com.videogen.segmentation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "script_segments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", nullable = false)
    @JsonIgnore
    private NarrationScript script;

    @Column(name = "segment_number", nullable = false)
    private Integer segmentNumber;

    @Column(name = "segment_text", nullable = false, columnDefinition = "TEXT")
    private String segmentText;

    @Column(name = "estimated_duration_seconds", nullable = false)
    private Double estimatedDurationSeconds;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
