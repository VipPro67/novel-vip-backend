package com.novel.vippro.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.novel.vippro.DTO.Admin.DashboardStatsDTO;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.CommentRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CommentRepository commentRepository;

    public DashboardStatsDTO getDashboardStats() {
        Instant now = Instant.now();
        Instant last24Hours = now.minus(24, ChronoUnit.HOURS);

        long totalUsers = userRepository.count();
        long newUsersToday = userRepository.countByCreatedAtAfter(last24Hours);
        
        long totalNovels = novelRepository.count();
        long popularNovels = novelRepository.countByRatingGreaterThanEqual(4); 
        
        long totalChapters = chapterRepository.count();
        long chaptersToday = chapterRepository.countByCreatedAtAfter(last24Hours);
        
        Long totalViewsObj = novelRepository.sumTotalViews();
        long totalViews = totalViewsObj != null ? totalViewsObj : 0;
        
        long viewsGrowth = 12; // mock percentage
        long activeUsers = Math.max(1, newUsersToday * 2); // basic mock formula
        
        double avgRating = 4.2; // mock average
        
        long commentsToday = commentRepository.countByCreatedAtAfter(last24Hours);
        long commentsGrowth = 8; // mock

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .newUsersToday(newUsersToday)
                .totalNovels(totalNovels)
                .popularNovels(popularNovels)
                .totalChapters(totalChapters)
                .chaptersToday(chaptersToday)
                .totalViews(totalViews)
                .viewsGrowth(viewsGrowth)
                .activeUsers(activeUsers)
                .avgRating(avgRating)
                .commentsToday(commentsToday)
                .commentsGrowth(commentsGrowth)
                .build();
    }
}
