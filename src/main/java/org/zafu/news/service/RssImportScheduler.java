package org.zafu.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.zafu.news.exception.RssImportInProgressException;

@Slf4j
@RequiredArgsConstructor
public class RssImportScheduler {
    private final RssImportService importService;

    @Scheduled(cron = "${app.ingestion.scheduling.cron:0 */30 * * * *}",
            zone = "${app.ingestion.scheduling.zone:Asia/Ho_Chi_Minh}")
    public void importFeed() {
        try {
            var result = importService.importFeed();
            log.info("Scheduled RSS import run {} finished: {}, inserted={}, duplicates={}, invalid={}, failed={}",
                    result.runId(), result.runStatus(), result.insertedCount(), result.duplicateCount(),
                    result.invalidCount(), result.failedCount());
        } catch (RssImportInProgressException exception) {
            log.info("Skipping scheduled RSS import because another import is running");
        } catch (RuntimeException exception) {
            log.error("Scheduled RSS import failed; next attempt follows the configured schedule", exception);
        }
    }
}
