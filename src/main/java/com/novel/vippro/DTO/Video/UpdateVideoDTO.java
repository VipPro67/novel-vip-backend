package com.novel.vippro.DTO.Video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateVideoDTO(
    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 4000)
    String description,

    @NotBlank
    @Size(max = 1024)
    String videoUrl,

    UUID videoSeriesId
) {}
