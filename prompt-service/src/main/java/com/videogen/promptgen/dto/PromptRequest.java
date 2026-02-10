package com.videogen.promptgen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {

    @NotEmpty(message = "Segments list is required and must not be empty")
    private List<@NotBlank(message = "Each segment must not be blank") String> segments;

    @NotBlank(message = "Style is required")
    private String style;

    private String customStyleDescription;
}
