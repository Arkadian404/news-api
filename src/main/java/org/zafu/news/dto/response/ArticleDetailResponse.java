package org.zafu.news.dto.response;

import java.time.Instant;

public record ArticleDetailResponse(Long id, String title, String source, String sourceUrl,
                                    String category, String description, Instant publishedAt,
                                    Instant createdAt, Instant updatedAt) {
}
