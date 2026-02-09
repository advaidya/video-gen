package com.videogen.segmentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Raw text is required")
    private String rawText;
}
