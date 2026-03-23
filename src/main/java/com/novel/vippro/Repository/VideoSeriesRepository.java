package com.novel.vippro.Repository;

import com.novel.vippro.Models.VideoSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoSeriesRepository extends JpaRepository<VideoSeries, UUID> {
    Page<VideoSeries> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);
}
