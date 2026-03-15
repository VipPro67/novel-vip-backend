package com.novel.vippro.DTO.Novel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelStatsDTO {
    // Hot novels stats
    private long mostViewedWeek;
    private long risingStars;
    private double popularAvgRating;
    private long hotStreakDays;
    
    // Top rated novels stats
    private long fiveStarNovels;
    private long fourPlusStarNovels;
    private long hallOfFame;
    private double highlyRatedAvg;
    
    // Latest updates stats
    private long updatedToday;
    private long updatedThisWeek;
    private long regularUpdates;
    private long freshChapters;
}
