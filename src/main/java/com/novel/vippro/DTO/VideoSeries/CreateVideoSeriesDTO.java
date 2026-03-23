package com.novel.vippro.DTO.VideoSeries;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVideoSeriesDTO(
    @NotBlank
    @Size(max = 255)
    String title,
    
    @Size(max = 4000)
    String description
) {}
