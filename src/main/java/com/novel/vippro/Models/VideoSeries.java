package com.novel.vippro.Models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.novel.vippro.Models.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "video_series")
@Getter
@Setter
@NoArgsConstructor
public class VideoSeries extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "videoSeries", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("video-series-videos")
    private List<Video> videos = new ArrayList<>();

    @ManyToMany(mappedBy = "videoSeriesList", fetch = FetchType.LAZY)
    private Set<Novel> novels = new HashSet<>();
}
