package com.videogen.segmentation.service.impl;

import com.videogen.segmentation.dto.ScriptRequest;
import com.videogen.segmentation.dto.ScriptResponse;
import com.videogen.segmentation.exception.ResourceNotFoundException;
import com.videogen.segmentation.model.NarrationScript;
import com.videogen.segmentation.model.ScriptSegment;
import com.videogen.segmentation.repository.NarrationScriptRepository;
import com.videogen.segmentation.repository.ScriptSegmentRepository;
import com.videogen.segmentation.service.SegmentationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SegmentationServiceImpl implements SegmentationService {

    private static final double WORDS_PER_SECOND = 2.5;
    private static final double TARGET_DURATION = 8.0;
    private static final int TARGET_WORDS = 20;

    private final NarrationScriptRepository scriptRepository;
    private final ScriptSegmentRepository segmentRepository;

    public SegmentationServiceImpl(NarrationScriptRepository scriptRepository,
                                   ScriptSegmentRepository segmentRepository) {
        this.scriptRepository = scriptRepository;
        this.segmentRepository = segmentRepository;
    }

    @Override
    public ScriptResponse createScript(ScriptRequest request) {
        NarrationScript script = NarrationScript.builder()
                .title(request.getTitle())
                .rawText(request.getRawText())
                .build();

        script = scriptRepository.save(script);

        List<ScriptSegment> segments = segmentText(request.getRawText(), script);
        script.setSegments(segments);
        script = scriptRepository.save(script);

        return ScriptResponse.fromEntity(script);
    }

    @Override
    @Transactional(readOnly = true)
    public ScriptResponse getScript(Long id) {
        NarrationScript script = scriptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Script", id));
        return ScriptResponse.fromEntity(script);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScriptResponse> getAllScripts() {
        return scriptRepository.findAll().stream()
                .map(ScriptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ScriptResponse updateScript(Long id, ScriptRequest request) {
        NarrationScript script = scriptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Script", id));

        script.setTitle(request.getTitle());
        script.setRawText(request.getRawText());

        // Clear existing segments and re-segment
        script.getSegments().clear();
        scriptRepository.saveAndFlush(script);

        List<ScriptSegment> newSegments = segmentText(request.getRawText(), script);
        script.getSegments().addAll(newSegments);
        script = scriptRepository.save(script);

        return ScriptResponse.fromEntity(script);
    }

    @Override
    public void deleteScript(Long id) {
        if (!scriptRepository.existsById(id)) {
            throw new ResourceNotFoundException("Script", id);
        }
        scriptRepository.deleteById(id);
    }

    /**
     * Splits text into segments of approximately TARGET_WORDS words,
     * respecting sentence boundaries.
     */
    public List<ScriptSegment> segmentText(String text, NarrationScript script) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        // Split into sentences
        String[] sentences = text.split("(?<=[.!?])\\s+");
        List<ScriptSegment> segments = new ArrayList<>();

        StringBuilder currentSegment = new StringBuilder();
        int currentWordCount = 0;
        int segmentNumber = 1;

        for (String sentence : sentences) {
            int sentenceWordCount = countWords(sentence);

            if (currentWordCount > 0 && currentWordCount + sentenceWordCount > TARGET_WORDS) {
                // Finalize the current segment
                segments.add(buildSegment(script, segmentNumber, currentSegment.toString().trim(), currentWordCount));
                segmentNumber++;
                currentSegment = new StringBuilder();
                currentWordCount = 0;
            }

            if (currentSegment.length() > 0) {
                currentSegment.append(" ");
            }
            currentSegment.append(sentence);
            currentWordCount += sentenceWordCount;
        }

        // Add the last segment if there's remaining text
        if (currentWordCount > 0) {
            segments.add(buildSegment(script, segmentNumber, currentSegment.toString().trim(), currentWordCount));
        }

        return segments;
    }

    private ScriptSegment buildSegment(NarrationScript script, int segmentNumber, String text, int wordCount) {
        double estimatedDuration = wordCount / WORDS_PER_SECOND;
        return ScriptSegment.builder()
                .script(script)
                .segmentNumber(segmentNumber)
                .segmentText(text)
                .estimatedDurationSeconds(estimatedDuration)
                .wordCount(wordCount)
                .build();
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
