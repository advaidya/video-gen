package com.videogen.promptgen.service;

import com.videogen.promptgen.client.ClaudeApiClient;
import com.videogen.promptgen.dto.PromptRequest;
import com.videogen.promptgen.dto.PromptResponse;
import com.videogen.promptgen.exception.ResourceNotFoundException;
import com.videogen.promptgen.model.ArtStyle;
import com.videogen.promptgen.model.JobStatus;
import com.videogen.promptgen.model.PromptJob;
import com.videogen.promptgen.repository.PromptJobRepository;
import com.videogen.promptgen.service.impl.PromptGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptGenerationServiceTest {

    @Mock
    private PromptJobRepository jobRepository;

    @Mock
    private ClaudeApiClient claudeApiClient;

    @InjectMocks
    private PromptGenerationServiceImpl service;

    @Test
    void createPromptJob_cinematicStyle_shouldReturnCompletedWith2Results() {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of("A hero walks through a misty forest.", "The castle looms in the distance."))
                .style("CINEMATIC")
                .build();

        when(jobRepository.save(any(PromptJob.class))).thenAnswer(invocation -> {
            PromptJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(1L);
            }
            if (job.getResults() == null) {
                job.setResults(new ArrayList<>());
            }
            return job;
        });

        when(claudeApiClient.generateImagePrompt(anyString(), eq(ArtStyle.CINEMATIC), isNull()))
                .thenReturn("A cinematic wide shot of a hero...");

        PromptResponse response = service.createPromptJob(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getSegmentNumber()).isEqualTo(1);
        assertThat(response.getResults().get(1).getSegmentNumber()).isEqualTo(2);
        verify(claudeApiClient, times(2)).generateImagePrompt(anyString(), eq(ArtStyle.CINEMATIC), isNull());
    }

    @Test
    void createPromptJob_customStyleWithDescription_shouldCallClientWithCustomDesc() {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of("A scene in the rain."))
                .style("CUSTOM")
                .customStyleDescription("Dark gothic horror style with muted colors")
                .build();

        when(jobRepository.save(any(PromptJob.class))).thenAnswer(invocation -> {
            PromptJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(1L);
            }
            if (job.getResults() == null) {
                job.setResults(new ArrayList<>());
            }
            return job;
        });

        when(claudeApiClient.generateImagePrompt(anyString(), eq(ArtStyle.CUSTOM), eq("Dark gothic horror style with muted colors")))
                .thenReturn("A dark gothic scene...");

        PromptResponse response = service.createPromptJob(request);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getStyle()).isEqualTo("CUSTOM");
        assertThat(response.getCustomStyleDescription()).isEqualTo("Dark gothic horror style with muted colors");
        verify(claudeApiClient).generateImagePrompt(anyString(), eq(ArtStyle.CUSTOM), eq("Dark gothic horror style with muted colors"));
    }

    @Test
    void createPromptJob_customWithoutDescription_shouldThrowIllegalArgumentException() {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of("A scene."))
                .style("CUSTOM")
                .build();

        assertThatThrownBy(() -> service.createPromptJob(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customStyleDescription");
    }

    @Test
    void createPromptJob_invalidStyle_shouldThrowIllegalArgumentException() {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of("A scene."))
                .style("INVALID_STYLE")
                .build();

        assertThatThrownBy(() -> service.createPromptJob(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid art style");
    }

    @Test
    void createPromptJob_singleSegment_shouldReturn1Result() {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of("A lone wanderer crosses the desert."))
                .style("ANIME")
                .build();

        when(jobRepository.save(any(PromptJob.class))).thenAnswer(invocation -> {
            PromptJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(1L);
            }
            if (job.getResults() == null) {
                job.setResults(new ArrayList<>());
            }
            return job;
        });

        when(claudeApiClient.generateImagePrompt(anyString(), eq(ArtStyle.ANIME), isNull()))
                .thenReturn("An anime-style wanderer...");

        PromptResponse response = service.createPromptJob(request);

        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getSegmentNumber()).isEqualTo(1);
    }

    @Test
    void createPromptJob_claudeApiFailure_shouldSetJobFailed() {
        PromptRequest request = PromptRequest.builder()
                .segments(List.of("A hero walks.", "A villain appears."))
                .style("PIXAR")
                .build();

        when(jobRepository.save(any(PromptJob.class))).thenAnswer(invocation -> {
            PromptJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(1L);
            }
            if (job.getResults() == null) {
                job.setResults(new ArrayList<>());
            }
            return job;
        });

        when(claudeApiClient.generateImagePrompt(anyString(), eq(ArtStyle.PIXAR), isNull()))
                .thenThrow(new RuntimeException("API connection failed"));

        PromptResponse response = service.createPromptJob(request);

        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getGeneratedPrompt()).isNull();
        assertThat(response.getResults().get(1).getGeneratedPrompt()).isNull();
    }

    @Test
    void getPromptJob_existingId_shouldReturnResponse() {
        PromptJob job = PromptJob.builder()
                .id(1L)
                .style(ArtStyle.CINEMATIC)
                .status(JobStatus.COMPLETED)
                .results(new ArrayList<>())
                .build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        PromptResponse response = service.getPromptJob(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStyle()).isEqualTo("CINEMATIC");
    }

    @Test
    void getPromptJob_nonExistingId_shouldThrowResourceNotFoundException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPromptJob(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllPromptJobs_shouldReturnList() {
        PromptJob job1 = PromptJob.builder().id(1L).style(ArtStyle.CINEMATIC).status(JobStatus.COMPLETED).results(new ArrayList<>()).build();
        PromptJob job2 = PromptJob.builder().id(2L).style(ArtStyle.GHIBLI).status(JobStatus.COMPLETED).results(new ArrayList<>()).build();

        when(jobRepository.findAll()).thenReturn(List.of(job1, job2));

        List<PromptResponse> responses = service.getAllPromptJobs();

        assertThat(responses).hasSize(2);
    }

    @Test
    void deletePromptJob_existingId_shouldDelete() {
        when(jobRepository.existsById(1L)).thenReturn(true);

        service.deletePromptJob(1L);

        verify(jobRepository).deleteById(1L);
    }

    @Test
    void deletePromptJob_nonExistingId_shouldThrowResourceNotFoundException() {
        when(jobRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePromptJob(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
