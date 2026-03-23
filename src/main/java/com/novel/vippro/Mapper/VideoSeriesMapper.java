package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.VideoSeries.CreateVideoSeriesDTO;
import com.novel.vippro.DTO.VideoSeries.UpdateVideoSeriesDTO;
import com.novel.vippro.DTO.VideoSeries.VideoSeriesDTO;
import com.novel.vippro.Models.VideoSeries;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface VideoSeriesMapper {

    VideoSeriesDTO VideoSeriesToDTO(VideoSeries videoSeries);
    
    VideoSeries CreateVideoSeriesDTOtoVideoSeries(CreateVideoSeriesDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateVideoSeriesFromDTO(UpdateVideoSeriesDTO dto, @MappingTarget VideoSeries videoSeries);
}
