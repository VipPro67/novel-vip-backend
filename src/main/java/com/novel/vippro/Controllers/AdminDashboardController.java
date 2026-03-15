package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Admin.DashboardStatsDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Services.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Operation(summary = "Get Dashboard Stats", description = "Retrieve statistics for the admin dashboard")
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR') or hasRole('MODERATOR')")
    public ControllerResponse<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = adminDashboardService.getDashboardStats();
        return ControllerResponse.success("Stats retrieved successfully", stats);
    }
}
