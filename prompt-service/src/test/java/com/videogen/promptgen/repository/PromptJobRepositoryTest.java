package com.videogen.promptgen.repository;

import com.videogen.promptgen.model.ArtStyle;
import com.videogen.promptgen.model.JobStatus;
import com.videogen.promptgen.model.PromptJob;
import com.videogen.promptgen.model.PromptResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
class PromptJobRepositoryTest {

    @Autowired
    private PromptJobRepository jobRepository;

    @Autowired
    private PromptResultRepository resultRepository;

    @Test
    void saveAndFind_shouldPersistPromptJob() {
        PromptJob job = PromptJob.builder()
                .style(ArtStyle.CINEMATIC)
                .status(JobStatus.COMPLETED)
                .build();

        PromptJob saved = jobRepository.save(job);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStyle()).isEqualTo(ArtStyle.CINEMATIC);

        Optional<PromptJob> found = jobRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void cascadeDelete_shouldRemoveResults() {
        PromptJob job = PromptJob.builder()
                .style(ArtStyle.GHIBLI)
                .status(JobStatus.COMPLETED)
                .build();
        job = jobRepository.save(job);

        PromptResult result1 = PromptResult.builder()
                .job(job)
                .segmentNumber(1)
                .segmentText("First segment.")
                .generatedPrompt("A Ghibli-style scene...")
                .build();

        PromptResult result2 = PromptResult.builder()
                .job(job)
                .segmentNumber(2)
                .segmentText("Second segment.")
                .generatedPrompt("Another Ghibli scene...")
                .build();

        job.getResults().add(result1);
        job.getResults().add(result2);
        jobRepository.save(job);

        assertThat(resultRepository.findByJobIdOrderBySegmentNumberAsc(job.getId())).hasSize(2);

        jobRepository.deleteById(job.getId());
        jobRepository.flush();

        assertThat(resultRepository.findByJobIdOrderBySegmentNumberAsc(job.getId())).isEmpty();
    }

    @Test
    void findResultsByJobId_shouldReturnOrderedBySegmentNumber() {
        PromptJob job = PromptJob.builder()
                .style(ArtStyle.PIXAR)
                .status(JobStatus.COMPLETED)
                .build();
        job = jobRepository.save(job);

        PromptResult result3 = PromptResult.builder()
                .job(job)
                .segmentNumber(3)
                .segmentText("Third segment.")
                .generatedPrompt("Pixar prompt 3")
                .build();

        PromptResult result1 = PromptResult.builder()
                .job(job)
                .segmentNumber(1)
                .segmentText("First segment.")
                .generatedPrompt("Pixar prompt 1")
                .build();

        PromptResult result2 = PromptResult.builder()
                .job(job)
                .segmentNumber(2)
                .segmentText("Second segment.")
                .generatedPrompt("Pixar prompt 2")
                .build();

        // Add in non-sequential order
        job.getResults().add(result3);
        job.getResults().add(result1);
        job.getResults().add(result2);
        jobRepository.save(job);

        List<PromptResult> results = resultRepository.findByJobIdOrderBySegmentNumberAsc(job.getId());

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getSegmentNumber()).isEqualTo(1);
        assertThat(results.get(1).getSegmentNumber()).isEqualTo(2);
        assertThat(results.get(2).getSegmentNumber()).isEqualTo(3);
    }
}
