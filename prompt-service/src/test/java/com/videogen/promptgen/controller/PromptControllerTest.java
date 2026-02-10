package com.videogen.promptgen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videogen.promptgen.dto.PromptRequest;
import com.videogen.promptgen.dto.PromptResponse;
import com.videogen.promptgen.dto.PromptResultResponse;
import com.videogen.promptgen.exception.ResourceNotFoundException;
import com.videogen.promptgen.service.PromptGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromptController.class)
class PromptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromptGenerationService promptGenerationService;

    @Test
    void createPromptJob_validRequest_shouldReturn201() throws Exception {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of("A hero walks through a misty forest."))
                .style("CINEMATIC")
                .build();

        PromptResponse response = PromptResponse.builder()
                .id(1L)
                .style("CINEMATIC")
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .results(List.of(
                        PromptResultResponse.builder()
                                .id(1L)
                                .segmentNumber(1)
                                .segmentText("A hero walks through a misty forest.")
                                .generatedPrompt("A cinematic wide shot...")
                                .createdAt(LocalDateTime.now())
                                .build()
                ))
                .build();

        when(promptGenerationService.createPromptJob(any(PromptRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.style").value("CINEMATIC"))
                .andExpect(jsonPath("$.results[0].segmentNumber").value(1));
    }

    @Test
    void createPromptJob_missingSegments_shouldReturn400() throws Exception {
        String json = "{\"style\":\"CINEMATIC\"}";

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPromptJob_emptySegmentsList_shouldReturn400() throws Exception {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of())
                .style("CINEMATIC")
                .build();

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPromptJob_missingStyle_shouldReturn400() throws Exception {
        String json = "{\"segments\":[\"A scene.\"]}";

        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPromptJob_existingId_shouldReturn200() throws Exception {
        PromptResponse response = PromptResponse.builder()
                .id(1L)
                .style("CINEMATIC")
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .results(List.of())
                .build();

        when(promptGenerationService.getPromptJob(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/prompts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.style").value("CINEMATIC"));
    }

    @Test
    void getPromptJob_nonExistingId_shouldReturn404() throws Exception {
        when(promptGenerationService.getPromptJob(99L))
                .thenThrow(new ResourceNotFoundException("PromptJob", 99L));

        mockMvc.perform(get("/api/v1/prompts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPromptJobs_shouldReturn200() throws Exception {
        List<PromptResponse> responses = List.of(
                PromptResponse.builder().id(1L).style("CINEMATIC").status("COMPLETED").results(List.of()).build(),
                PromptResponse.builder().id(2L).style("GHIBLI").status("COMPLETED").results(List.of()).build()
        );

        when(promptGenerationService.getAllPromptJobs()).thenReturn(responses);

        mockMvc.perform(get("/api/v1/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deletePromptJob_shouldReturn204() throws Exception {
        doNothing().when(promptGenerationService).deletePromptJob(1L);

        mockMvc.perform(delete("/api/v1/prompts/1"))
                .andExpect(status().isNoContent());

        verify(promptGenerationService).deletePromptJob(1L);
    }
}
