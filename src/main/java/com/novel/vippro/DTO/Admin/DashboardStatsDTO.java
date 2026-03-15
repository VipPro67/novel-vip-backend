package com.novel.vippro.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalUsers;
    private long newUsersToday;
    private long totalNovels;
    private long popularNovels;
    private long totalChapters;
    private long chaptersToday;
    private long totalViews;
    private long viewsGrowth;
    private long activeUsers;
    private double avgRating;
    private long commentsToday;
    private long commentsGrowth;
}
