package com.videogen.promptgen.service.impl;

import com.videogen.promptgen.client.ClaudeApiClient;
import com.videogen.promptgen.dto.PromptRequest;
import com.videogen.promptgen.dto.PromptResponse;
import com.videogen.promptgen.exception.ResourceNotFoundException;
import com.videogen.promptgen.model.ArtStyle;
import com.videogen.promptgen.model.JobStatus;
import com.videogen.promptgen.model.PromptJob;
import com.videogen.promptgen.model.PromptResult;
import com.videogen.promptgen.repository.PromptJobRepository;
import com.videogen.promptgen.service.PromptGenerationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromptGenerationServiceImpl implements PromptGenerationService {

    private final PromptJobRepository jobRepository;
    private final ClaudeApiClient claudeApiClient;

    public PromptGenerationServiceImpl(PromptJobRepository jobRepository,
                                       ClaudeApiClient claudeApiClient) {
        this.jobRepository = jobRepository;
        this.claudeApiClient = claudeApiClient;
    }

    @Override
    public PromptResponse createPromptJob(PromptRequest request) {
        // Parse style
        ArtStyle style;
        try {
            style = ArtStyle.valueOf(request.getStyle().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid art style: " + request.getStyle()
                    + ". Valid styles are: CINEMATIC, GHIBLI, PIXAR, WATERCOLOR, PHOTOREALISTIC, ANIME, CUSTOM");
        }

        // Validate CUSTOM requires description
        if (style == ArtStyle.CUSTOM &&
                (request.getCustomStyleDescription() == null || request.getCustomStyleDescription().isBlank())) {
            throw new IllegalArgumentException("Custom style requires a customStyleDescription");
        }

        // Create job
        PromptJob job = PromptJob.builder()
                .style(style)
                .customStyleDescription(request.getCustomStyleDescription())
                .status(JobStatus.PENDING)
                .build();
        job = jobRepository.save(job);

        // Set processing
        job.setStatus(JobStatus.PROCESSING);
        job = jobRepository.save(job);

        // Process segments
        List<PromptResult> results = new ArrayList<>();
        boolean hasFailed = false;

        for (int i = 0; i < request.getSegments().size(); i++) {
            String segmentText = request.getSegments().get(i);
            String generatedPrompt = null;

            try {
                generatedPrompt = claudeApiClient.generateImagePrompt(
                        segmentText, style, request.getCustomStyleDescription());
            } catch (Exception e) {
                hasFailed = true;
            }

            PromptResult result = PromptResult.builder()
                    .job(job)
                    .segmentNumber(i + 1)
                    .segmentText(segmentText)
                    .generatedPrompt(generatedPrompt)
                    .build();
            results.add(result);
        }

        job.getResults().addAll(results);
        job.setStatus(hasFailed ? JobStatus.FAILED : JobStatus.COMPLETED);
        job = jobRepository.save(job);

        return PromptResponse.fromEntity(job);
    }

    @Override
    @Transactional(readOnly = true)
    public PromptResponse getPromptJob(Long id) {
        PromptJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromptJob", id));
        return PromptResponse.fromEntity(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromptResponse> getAllPromptJobs() {
        return jobRepository.findAll().stream()
                .map(PromptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePromptJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("PromptJob", id);
        }
        jobRepository.deleteById(id);
    }
}
