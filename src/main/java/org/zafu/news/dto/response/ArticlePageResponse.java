package org.zafu.news.dto.response;

import java.util.List;

public record ArticlePageResponse(List<ArticleResponse> items, int page, int size,
                                  long totalElements, int totalPages) {
}
