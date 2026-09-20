package org.zafu.news.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.zafu.news.dto.response.ApiResponse;
import org.zafu.news.dto.response.IngestionRunPageResponse;
import org.zafu.news.dto.response.IngestionRunResponse;
import org.zafu.news.service.IngestionRunService;

@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/dev/ingestion-runs")
public class IngestionRunController {
    private final IngestionRunService service;

    @GetMapping
    public ApiResponse<IngestionRunPageResponse> getRuns(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(service.getRuns(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<IngestionRunResponse> getRun(@PathVariable @Positive Long id) {
        return ApiResponse.ok(service.getById(id));
    }
}
