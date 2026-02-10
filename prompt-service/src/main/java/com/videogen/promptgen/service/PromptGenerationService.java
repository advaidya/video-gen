package com.videogen.promptgen.service;

import com.videogen.promptgen.dto.PromptRequest;
import com.videogen.promptgen.dto.PromptResponse;

import java.util.List;

public interface PromptGenerationService {

    PromptResponse createPromptJob(PromptRequest request);

    PromptResponse getPromptJob(Long id);

    List<PromptResponse> getAllPromptJobs();

    void deletePromptJob(Long id);
}
