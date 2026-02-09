package com.videogen.segmentation.repository;

import com.videogen.segmentation.model.NarrationScript;
import com.videogen.segmentation.model.ScriptSegment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
class NarrationScriptRepositoryTest {

    @Autowired
    private NarrationScriptRepository scriptRepository;

    @Autowired
    private ScriptSegmentRepository segmentRepository;

    @Test
    void saveAndFind_shouldPersistScript() {
        NarrationScript script = NarrationScript.builder()
                .title("Test Script")
                .rawText("This is test text.")
                .build();

        NarrationScript saved = scriptRepository.save(script);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test Script");

        Optional<NarrationScript> found = scriptRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRawText()).isEqualTo("This is test text.");
    }

    @Test
    void cascadeDelete_shouldRemoveSegments() {
        NarrationScript script = NarrationScript.builder()
                .title("Script with Segments")
                .rawText("Text content.")
                .build();

        script = scriptRepository.save(script);

        ScriptSegment segment1 = ScriptSegment.builder()
                .script(script)
                .segmentNumber(1)
                .segmentText("First segment.")
                .estimatedDurationSeconds(2.0)
                .wordCount(2)
                .build();

        ScriptSegment segment2 = ScriptSegment.builder()
                .script(script)
                .segmentNumber(2)
                .segmentText("Second segment.")
                .estimatedDurationSeconds(2.0)
                .wordCount(2)
                .build();

        script.getSegments().add(segment1);
        script.getSegments().add(segment2);
        scriptRepository.save(script);

        assertThat(segmentRepository.findByScriptIdOrderBySegmentNumberAsc(script.getId())).hasSize(2);

        scriptRepository.deleteById(script.getId());
        scriptRepository.flush();

        assertThat(segmentRepository.findByScriptIdOrderBySegmentNumberAsc(script.getId())).isEmpty();
    }

    @Test
    void findSegmentsByScriptId_shouldReturnOrderedSegments() {
        NarrationScript script = NarrationScript.builder()
                .title("Ordered Script")
                .rawText("Ordered text.")
                .build();

        script = scriptRepository.save(script);

        ScriptSegment segment3 = ScriptSegment.builder()
                .script(script)
                .segmentNumber(3)
                .segmentText("Third segment.")
                .estimatedDurationSeconds(2.0)
                .wordCount(2)
                .build();

        ScriptSegment segment1 = ScriptSegment.builder()
                .script(script)
                .segmentNumber(1)
                .segmentText("First segment.")
                .estimatedDurationSeconds(2.0)
                .wordCount(2)
                .build();

        ScriptSegment segment2 = ScriptSegment.builder()
                .script(script)
                .segmentNumber(2)
                .segmentText("Second segment.")
                .estimatedDurationSeconds(2.0)
                .wordCount(2)
                .build();

        // Add in non-sequential order
        script.getSegments().add(segment3);
        script.getSegments().add(segment1);
        script.getSegments().add(segment2);
        scriptRepository.save(script);

        List<ScriptSegment> segments = segmentRepository.findByScriptIdOrderBySegmentNumberAsc(script.getId());

        assertThat(segments).hasSize(3);
        assertThat(segments.get(0).getSegmentNumber()).isEqualTo(1);
        assertThat(segments.get(1).getSegmentNumber()).isEqualTo(2);
        assertThat(segments.get(2).getSegmentNumber()).isEqualTo(3);
    }
}
