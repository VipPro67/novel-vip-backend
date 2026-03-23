package com.novel.vippro.DTO.VideoSeries;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record VideoSeriesDTO(
    UUID id,
    String title,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
