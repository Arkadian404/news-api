package org.zafu.news.dto.response;

import org.zafu.news.model.IngestionStatus;

import java.time.Instant;

public record IngestionRunResponse(Long id, String source, String feedUrl, IngestionStatus runStatus,
                                    Instant startedAt, Instant finishedAt, int totalItems, int insertedCount,
                                    int duplicateCount, int invalidCount, int failedCount,
                                    String errorCode, String errorMessage) {
}
