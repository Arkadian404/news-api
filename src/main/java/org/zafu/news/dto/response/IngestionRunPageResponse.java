package org.zafu.news.dto.response;

import java.util.List;

public record IngestionRunPageResponse(List<IngestionRunResponse> items, int page, int size,
                                       long totalElements, int totalPages) {
}
