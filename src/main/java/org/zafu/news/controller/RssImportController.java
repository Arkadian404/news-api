package org.zafu.news.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zafu.news.dto.response.ApiResponse;
import org.zafu.news.dto.response.RssImportResponse;
import org.zafu.news.service.RssImportService;

@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/dev/rss-import")
public class RssImportController {
    private final RssImportService rssImportService;

    @PostMapping
    public ApiResponse<RssImportResponse> importFeed() {
        return ApiResponse.ok(rssImportService.importFeed());
    }
}
