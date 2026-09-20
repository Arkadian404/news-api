package org.zafu.news.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zafu.news.client.RssClient;
import org.zafu.news.dto.response.ApiResponse;
import org.zafu.news.dto.response.RssItemResponse;
import org.zafu.news.parser.RssParser;

import java.util.List;

@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/dev/rss-items")
public class RssItemsController {
    private final RssClient rssClient;
    private final RssParser rssParser;

    @GetMapping
    public ApiResponse<List<RssItemResponse>> getItems() {
        return ApiResponse.ok(rssParser.parse(rssClient.fetch()));
    }
}
