package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.VideoSeries.CreateVideoSeriesDTO;
import com.novel.vippro.DTO.VideoSeries.UpdateVideoSeriesDTO;
import com.novel.vippro.DTO.VideoSeries.VideoSeriesDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.VideoSeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/video-series")
@Tag(name = "Video Series", description = "APIs for managing video series")
@SecurityRequirement(name = "bearerAuth")
public class VideoSeriesController {

    @Autowired
    private VideoSeriesService videoSeriesService;

    @Operation(summary = "Create video series", description = "Create a new video series (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video series created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ControllerResponse<VideoSeriesDTO> createVideoSeries(
            @Parameter(description = "Video series details", required = true) @Valid @RequestBody CreateVideoSeriesDTO request) {
        VideoSeriesDTO created = videoSeriesService.createVideoSeries(request);
        return ControllerResponse.success("Video series created successfully", created);
    }

    @Operation(summary = "Get video series list", description = "Retrieve a paginated list of video series")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video series retrieved successfully")
    })
    @GetMapping
    public ControllerResponse<PageResponse<VideoSeriesDTO>> getVideoSeriesList(
            @Parameter(description = "Search query to filter by title or description")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "12")
            @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        PageResponse<VideoSeriesDTO> series = videoSeriesService.getVideoSeriesList(search, pageable);
        return ControllerResponse.success("Video series retrieved successfully", series);
    }

    @Operation(summary = "Get video series by id", description = "Retrieve a single video series by its identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video series retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Video series not found")
    })
    @GetMapping("/{id}")
    public ControllerResponse<VideoSeriesDTO> getVideoSeries(
            @Parameter(description = "Video series identifier", required = true) @PathVariable UUID id) {
        VideoSeriesDTO series = videoSeriesService.getVideoSeries(id);
        return ControllerResponse.success("Video series retrieved successfully", series);
    }

    @Operation(summary = "Update video series", description = "Update an existing video series (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video series updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Video series not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ControllerResponse<VideoSeriesDTO> updateVideoSeries(
            @Parameter(description = "Video series identifier", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated video series details", required = true) @Valid @RequestBody UpdateVideoSeriesDTO request) {
        VideoSeriesDTO updated = videoSeriesService.updateVideoSeries(id, request);
        return ControllerResponse.success("Video series updated successfully", updated);
    }

    @Operation(summary = "Delete video series", description = "Delete an existing video series (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video series deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Video series not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ControllerResponse<Void> deleteVideoSeries(
            @Parameter(description = "Video series identifier", required = true) @PathVariable UUID id) {
        videoSeriesService.deleteVideoSeries(id);
        return ControllerResponse.success("Video series deleted successfully",null);
    }
}
