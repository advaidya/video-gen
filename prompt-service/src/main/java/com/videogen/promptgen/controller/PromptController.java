package com.videogen.promptgen.controller;

import com.videogen.promptgen.dto.PromptRequest;
import com.videogen.promptgen.dto.PromptResponse;
import com.videogen.promptgen.service.PromptGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prompts")
public class PromptController {

    private final PromptGenerationService promptGenerationService;

    public PromptController(PromptGenerationService promptGenerationService) {
        this.promptGenerationService = promptGenerationService;
    }

    @PostMapping
    public ResponseEntity<PromptResponse> createPromptJob(@Valid @RequestBody PromptRequest request) {
        PromptResponse response = promptGenerationService.createPromptJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptResponse> getPromptJob(@PathVariable Long id) {
        PromptResponse response = promptGenerationService.getPromptJob(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PromptResponse>> getAllPromptJobs() {
        List<PromptResponse> responses = promptGenerationService.getAllPromptJobs();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromptJob(@PathVariable Long id) {
        promptGenerationService.deletePromptJob(id);
        return ResponseEntity.noContent().build();
    }
}
