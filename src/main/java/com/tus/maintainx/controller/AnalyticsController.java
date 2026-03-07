package com.tus.maintainx.controller;

import com.tus.maintainx.dto.AnalyticsDashboardResponseDTO;
import com.tus.maintainx.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboard());
    }
}
