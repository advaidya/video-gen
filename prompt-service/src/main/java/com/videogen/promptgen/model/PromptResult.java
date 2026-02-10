package com.videogen.promptgen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore
    private PromptJob job;

    @Column(name = "segment_number", nullable = false)
    private Integer segmentNumber;

    @Column(name = "segment_text", nullable = false, columnDefinition = "TEXT")
    private String segmentText;

    @Column(name = "generated_prompt", columnDefinition = "TEXT")
    private String generatedPrompt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
