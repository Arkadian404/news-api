package org.zafu.news.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zafu.news.client.RssClient;
import org.zafu.news.dto.response.ApiResponse;
import org.zafu.news.dto.response.RssPreviewResponse;

import java.nio.charset.StandardCharsets;

@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/dev/rss-preview")
public class RssPreviewController {
    private final RssClient rssClient;

    @GetMapping
    public ApiResponse<RssPreviewResponse> preview() {
        byte[] content = rssClient.fetch();
        String xml = new String(content, StandardCharsets.UTF_8);
        int previewEnd = xml.offsetByCodePoints(0, Math.min(2000, xml.codePointCount(0, xml.length())));
        return ApiResponse.ok(new RssPreviewResponse(rssClient.getFeedUrl().toString(),
                content.length, xml.substring(0, previewEnd)));
    }
}
