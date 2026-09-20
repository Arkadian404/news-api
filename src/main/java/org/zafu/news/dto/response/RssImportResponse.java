package org.zafu.news.dto.response;

import org.zafu.news.model.IngestionStatus;

public record RssImportResponse(Long runId, IngestionStatus runStatus, int totalItems, int insertedCount, int duplicateCount,
                                int invalidCount, int failedCount) {
}
