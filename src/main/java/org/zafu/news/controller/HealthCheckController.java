package org.zafu.news.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zafu.news.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/health-check")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }

}
