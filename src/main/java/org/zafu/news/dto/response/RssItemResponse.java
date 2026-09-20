package org.zafu.news.dto.response;

import java.time.Instant;

public record RssItemResponse(String title, String sourceUrl, String description, Instant publishedAt) {
}
