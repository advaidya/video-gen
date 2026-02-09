package com.videogen.segmentation.controller;

import com.videogen.segmentation.dto.ScriptRequest;
import com.videogen.segmentation.dto.ScriptResponse;
import com.videogen.segmentation.service.SegmentationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scripts")
public class ScriptController {

    private final SegmentationService segmentationService;

    public ScriptController(SegmentationService segmentationService) {
        this.segmentationService = segmentationService;
    }

    @PostMapping
    public ResponseEntity<ScriptResponse> createScript(@Valid @RequestBody ScriptRequest request) {
        ScriptResponse response = segmentationService.createScript(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScriptResponse> getScript(@PathVariable Long id) {
        ScriptResponse response = segmentationService.getScript(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScriptResponse>> getAllScripts() {
        List<ScriptResponse> responses = segmentationService.getAllScripts();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScriptResponse> updateScript(@PathVariable Long id,
                                                       @Valid @RequestBody ScriptRequest request) {
        ScriptResponse response = segmentationService.updateScript(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable Long id) {
        segmentationService.deleteScript(id);
        return ResponseEntity.noContent().build();
    }
}
