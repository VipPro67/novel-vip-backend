package com.novel.vippro.Services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Repository.ViewStatRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.ViewStat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ViewStatService {

    private static final Logger logger = LogManager.getLogger(ViewStatService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ViewStatRepository viewStatRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private RedisViewStatService redisViewStatService;

    @Transactional
    public void recordView(UUID novelId, UUID chapterId) {
        UUID userId = null;
        try {
            userId = UserDetailsImpl.getCurrentUserId();
        } catch (Exception e) {
            // User not logged in, proceed as guest
        }
        
        LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);

        Long recentViews = 0L;
        if (userId != null) {
            recentViews = viewStatRepository.countRecentViewsByUserAndNovel(novelId, userId, threeMinutesAgo);
        }

        if (recentViews == 0) {
            ViewStat stat = new ViewStat();
            stat.setNovel(novelRepository.getReferenceById(novelId));
            if (chapterId != null) {
                stat.setChapter(chapterRepository.getReferenceById(chapterId));
            }
            if (userId != null) {
                stat.setUser(userRepository.getReferenceById(userId));
            }
            stat.setViewDate(LocalDateTime.now());

            viewStatRepository.save(stat);
            
            redisViewStatService.incrementViewDelta(novelId);
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void syncViewCountsToDb() {
        logger.info("Starting scheduled view count sync from Redis to DB...");
        Set<UUID> novelIds = redisViewStatService.getPendingSyncNovels();
        
        int syncedCount = 0;
        for (UUID novelId : novelIds) {
            try {
                Long delta = redisViewStatService.resetViewDelta(novelId);
                if (delta != null && delta > 0) {
                    applyViewDeltaToDb(novelId, delta);
                    syncedCount++;
                }
            } catch (Exception e) {
                logger.error("Failed to sync view counts for novel: " + novelId, e);
            }
        }
        
        if (syncedCount > 0) {
            logger.info("Successfully synced view counts for {} novels.", syncedCount);
        }
    }

    @Transactional
    public void applyViewDeltaToDb(UUID novelId, Long delta) {
        Novel novel = novelRepository.findById(novelId).orElseThrow();
        
        // Update total views
        novel.setTotalViews((novel.getTotalViews() != null ? novel.getTotalViews() : 0L) + delta);
        
        // Reset monthly/daily if needed
        LocalDateTime now = LocalDateTime.now();
        if (novel.getLastViewReset() == null || 
            !now.toLocalDate().equals(novel.getLastViewReset().toLocalDate())) {
            novel.setDailyViews(delta);
        } else {
            novel.setDailyViews((novel.getDailyViews() != null ? novel.getDailyViews() : 0L) + delta);
        }
        
        if (novel.getLastViewReset() == null || 
            now.getMonth() != novel.getLastViewReset().getMonth()) {
            novel.setMonthlyViews(delta);
        } else {
            novel.setMonthlyViews((novel.getMonthlyViews() != null ? novel.getMonthlyViews() : 0L) + delta);
        }
        
        novel.setLastViewReset(now);
        novelRepository.save(novel);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getNovelViewStats(UUID novelId) {
        Novel novel = novelRepository.findById(novelId).orElseThrow();
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", novel.getTotalViews() != null ? novel.getTotalViews() : 0L);
        stats.put("monthly", novel.getMonthlyViews() != null ? novel.getMonthlyViews() : 0L);
        stats.put("daily", novel.getDailyViews() != null ? novel.getDailyViews() : 0L);
        
        return stats;
    }
}
