package com.videogen.segmentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videogen.segmentation.dto.ScriptRequest;
import com.videogen.segmentation.dto.ScriptResponse;
import com.videogen.segmentation.dto.SegmentResponse;
import com.videogen.segmentation.exception.GlobalExceptionHandler;
import com.videogen.segmentation.exception.ResourceNotFoundException;
import com.videogen.segmentation.service.SegmentationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScriptController.class)
class ScriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SegmentationService segmentationService;

    @Test
    void createScript_validRequest_shouldReturn201() throws Exception {
        ScriptRequest request = ScriptRequest.builder()
                .title("Test Script")
                .rawText("This is a test narration.")
                .build();

        ScriptResponse response = ScriptResponse.builder()
                .id(1L)
                .title("Test Script")
                .rawText("This is a test narration.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .segments(List.of(
                        SegmentResponse.builder()
                                .id(1L)
                                .segmentNumber(1)
                                .segmentText("This is a test narration.")
                                .estimatedDurationSeconds(2.0)
                                .wordCount(5)
                                .build()
                ))
                .build();

        when(segmentationService.createScript(any(ScriptRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Script"))
                .andExpect(jsonPath("$.segments[0].segmentNumber").value(1));
    }

    @Test
    void createScript_missingTitle_shouldReturn400() throws Exception {
        ScriptRequest request = ScriptRequest.builder()
                .rawText("Some text.")
                .build();

        mockMvc.perform(post("/api/v1/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createScript_missingRawText_shouldReturn400() throws Exception {
        ScriptRequest request = ScriptRequest.builder()
                .title("Title Only")
                .build();

        mockMvc.perform(post("/api/v1/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getScript_existingId_shouldReturn200() throws Exception {
        ScriptResponse response = ScriptResponse.builder()
                .id(1L)
                .title("Test Script")
                .rawText("Text.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .segments(List.of())
                .build();

        when(segmentationService.getScript(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/scripts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Script"));
    }

    @Test
    void getScript_nonExistingId_shouldReturn404() throws Exception {
        when(segmentationService.getScript(99L))
                .thenThrow(new ResourceNotFoundException("Script", 99L));

        mockMvc.perform(get("/api/v1/scripts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllScripts_shouldReturn200() throws Exception {
        List<ScriptResponse> responses = List.of(
                ScriptResponse.builder()
                        .id(1L)
                        .title("Script 1")
                        .rawText("Text 1.")
                        .segments(List.of())
                        .build(),
                ScriptResponse.builder()
                        .id(2L)
                        .title("Script 2")
                        .rawText("Text 2.")
                        .segments(List.of())
                        .build()
        );

        when(segmentationService.getAllScripts()).thenReturn(responses);

        mockMvc.perform(get("/api/v1/scripts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateScript_validRequest_shouldReturn200() throws Exception {
        ScriptRequest request = ScriptRequest.builder()
                .title("Updated Title")
                .rawText("Updated text content.")
                .build();

        ScriptResponse response = ScriptResponse.builder()
                .id(1L)
                .title("Updated Title")
                .rawText("Updated text content.")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .segments(List.of())
                .build();

        when(segmentationService.updateScript(eq(1L), any(ScriptRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/scripts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteScript_shouldReturn204() throws Exception {
        doNothing().when(segmentationService).deleteScript(1L);

        mockMvc.perform(delete("/api/v1/scripts/1"))
                .andExpect(status().isNoContent());

        verify(segmentationService).deleteScript(1L);
    }
}
