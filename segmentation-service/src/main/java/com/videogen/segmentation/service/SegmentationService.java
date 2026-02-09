package com.videogen.segmentation.service;

import com.videogen.segmentation.dto.ScriptRequest;
import com.videogen.segmentation.dto.ScriptResponse;

import java.util.List;

public interface SegmentationService {

    ScriptResponse createScript(ScriptRequest request);

    ScriptResponse getScript(Long id);

    List<ScriptResponse> getAllScripts();

    ScriptResponse updateScript(Long id, ScriptRequest request);

    void deleteScript(Long id);
}
