package com.videogen.segmentation.service;

import com.videogen.segmentation.dto.ScriptRequest;
import com.videogen.segmentation.dto.ScriptResponse;
import com.videogen.segmentation.exception.ResourceNotFoundException;
import com.videogen.segmentation.model.NarrationScript;
import com.videogen.segmentation.model.ScriptSegment;
import com.videogen.segmentation.repository.NarrationScriptRepository;
import com.videogen.segmentation.repository.ScriptSegmentRepository;
import com.videogen.segmentation.service.impl.SegmentationServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegmentationServiceTest {

    @Mock
    private NarrationScriptRepository scriptRepository;

    @Mock
    private ScriptSegmentRepository segmentRepository;

    @InjectMocks
    private SegmentationServiceImpl segmentationService;

    @Test
    void createScript_shortText_shouldProduceOneSegment() {
        ScriptRequest request = ScriptRequest.builder()
                .title("Short Script")
                .rawText("This is a short narration text.")
                .build();

        NarrationScript savedScript = NarrationScript.builder()
                .id(1L)
                .title("Short Script")
                .rawText("This is a short narration text.")
                .segments(new ArrayList<>())
                .build();

        when(scriptRepository.save(any(NarrationScript.class))).thenReturn(savedScript);

        ScriptResponse response = segmentationService.createScript(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Short Script");
        verify(scriptRepository, times(2)).save(any(NarrationScript.class));
    }

    @Test
    void segmentText_longText_shouldProduceMultipleSegments() {
        // 60+ words across multiple sentences
        String longText = "The quick brown fox jumps over the lazy dog. " +
                "This sentence adds more words to the total count. " +
                "We need to make sure we have enough words for multiple segments. " +
                "Each segment should contain about twenty words. " +
                "The algorithm splits at sentence boundaries for natural reading. " +
                "Here is another sentence to push us past sixty words total. " +
                "And one more sentence to be absolutely sure we have enough content.";

        NarrationScript script = NarrationScript.builder()
                .id(1L)
                .title("Test")
                .rawText(longText)
                .segments(new ArrayList<>())
                .build();

        List<ScriptSegment> segments = segmentationService.segmentText(longText, script);

        assertThat(segments).hasSizeGreaterThanOrEqualTo(3);
        // Verify segment numbers are sequential
        for (int i = 0; i < segments.size(); i++) {
            assertThat(segments.get(i).getSegmentNumber()).isEqualTo(i + 1);
        }
    }

    @Test
    void segmentText_shouldRespectSentenceBoundaries() {
        String text = "First sentence here. Second sentence follows. Third one too. " +
                "Fourth sentence added. Fifth sentence now. Sixth sentence here. " +
                "Seventh sentence appears. Eighth sentence follows.";

        NarrationScript script = NarrationScript.builder()
                .id(1L)
                .title("Test")
                .rawText(text)
                .segments(new ArrayList<>())
                .build();

        List<ScriptSegment> segments = segmentationService.segmentText(text, script);

        // Each segment text should end with a sentence-ending punctuation
        for (ScriptSegment segment : segments) {
            String trimmed = segment.getSegmentText().trim();
            assertThat(trimmed).matches(".*[.!?]$");
        }
    }

    @Test
    void segmentText_emptyText_shouldReturnEmptyList() {
        NarrationScript script = NarrationScript.builder()
                .id(1L)
                .title("Test")
                .rawText("")
                .segments(new ArrayList<>())
                .build();

        List<ScriptSegment> segments = segmentationService.segmentText("", script);

        assertThat(segments).isEmpty();
    }

    @Test
    void segmentText_blankText_shouldReturnEmptyList() {
        NarrationScript script = NarrationScript.builder()
                .id(1L)
                .title("Test")
                .rawText("   ")
                .segments(new ArrayList<>())
                .build();

        List<ScriptSegment> segments = segmentationService.segmentText("   ", script);

        assertThat(segments).isEmpty();
    }

    @Test
    void segmentText_shouldCalculateWordCountAccurately() {
        String text = "One two three four five.";

        NarrationScript script = NarrationScript.builder()
                .id(1L)
                .title("Test")
                .rawText(text)
                .segments(new ArrayList<>())
                .build();

        List<ScriptSegment> segments = segmentationService.segmentText(text, script);

        assertThat(segments).hasSize(1);
        assertThat(segments.get(0).getWordCount()).isEqualTo(5);
    }

    @Test
    void segmentText_shouldCalculateDurationAccurately() {
        String text = "One two three four five.";

        NarrationScript script = NarrationScript.builder()
                .id(1L)
                .title("Test")
                .rawText(text)
                .segments(new ArrayList<>())
                .build();

        List<ScriptSegment> segments = segmentationService.segmentText(text, script);

        assertThat(segments).hasSize(1);
        // 5 words / 2.5 words per second = 2.0 seconds
        assertThat(segments.get(0).getEstimatedDurationSeconds()).isEqualTo(2.0);
    }

    @Test
    void getScript_existingId_shouldReturnScript() {
        NarrationScript script = NarrationScript.builder()
                .id(1L)
                .title("Test Script")
                .rawText("Some text.")
                .segments(new ArrayList<>())
                .build();

        when(scriptRepository.findById(1L)).thenReturn(Optional.of(script));

        ScriptResponse response = segmentationService.getScript(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Script");
    }

    @Test
    void getScript_nonExistingId_shouldThrowException() {
        when(scriptRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> segmentationService.getScript(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteScript_existingId_shouldDelete() {
        when(scriptRepository.existsById(1L)).thenReturn(true);

        segmentationService.deleteScript(1L);

        verify(scriptRepository).deleteById(1L);
    }

    @Test
    void deleteScript_nonExistingId_shouldThrowException() {
        when(scriptRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> segmentationService.deleteScript(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
