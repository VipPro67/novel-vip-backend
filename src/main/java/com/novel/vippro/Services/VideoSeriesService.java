package com.novel.vippro.Services;

import com.novel.vippro.DTO.VideoSeries.CreateVideoSeriesDTO;
import com.novel.vippro.DTO.VideoSeries.UpdateVideoSeriesDTO;
import com.novel.vippro.DTO.VideoSeries.VideoSeriesDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.VideoSeries;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.VideoSeriesRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class VideoSeriesService {

    @Autowired
    private VideoSeriesRepository videoSeriesRepository;

    @Autowired
    private Mapper mapper;

    @Transactional
    public VideoSeriesDTO createVideoSeries(CreateVideoSeriesDTO request) {
        VideoSeries series = mapper.CreateVideoSeriesDTOtoVideoSeries(request);
        VideoSeries saved = videoSeriesRepository.save(series);
        return mapper.VideoSeriesToDTO(saved);
    }

    @Transactional
    public VideoSeriesDTO updateVideoSeries(UUID id, UpdateVideoSeriesDTO request) {
        VideoSeries series = videoSeriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video Series not found"));
        
        mapper.updateVideoSeriesFromDTO(request, series);
        VideoSeries updated = videoSeriesRepository.save(series);
        return mapper.VideoSeriesToDTO(updated);
    }

    @Transactional
    public void deleteVideoSeries(UUID id) {
        VideoSeries series = videoSeriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video Series not found"));
        
        // Disassociate related novels before deleting
        if (series.getNovels() != null) {
            series.getNovels().forEach(novel -> novel.getVideoSeriesList().remove(series));
        }
        
        videoSeriesRepository.delete(series);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoSeriesDTO> getVideoSeriesList(String search, Pageable pageable) {
        Page<VideoSeries> page;
        if (StringUtils.hasText(search)) {
            page = videoSeriesRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        } else {
            page = videoSeriesRepository.findAll(pageable);
        }

        return new PageResponse<>(page.map(mapper::VideoSeriesToDTO));
    }

    @Transactional(readOnly = true)
    public VideoSeriesDTO getVideoSeries(UUID id) {
        VideoSeries series = videoSeriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video Series not found"));
        return mapper.VideoSeriesToDTO(series);
    }
}
