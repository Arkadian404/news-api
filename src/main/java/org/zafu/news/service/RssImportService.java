package org.zafu.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.zafu.news.client.RssClient;
import org.zafu.news.dto.response.RssImportResponse;
import org.zafu.news.parser.RssParser;
import org.zafu.news.repository.RssArticleWriter;
import org.zafu.news.exception.RssImportException;
import org.zafu.news.model.IngestionRun;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssImportService {
    private final RssClient rssClient;
    private final RssParser rssParser;
    private final RssArticleNormalizer normalizer;
    private final RssArticleWriter writer;
    private final IngestionRunService history;

    public RssImportResponse importFeed() {
        var run = history.start(rssClient.getFeedUrl().toString());
        try {
            processItems(run);
            history.complete(run);
            return new RssImportResponse(run.getId(), run.getRunStatus(), run.getTotalItems(),
                    run.getInsertedCount(), run.getDuplicateCount(), run.getInvalidCount(), run.getFailedCount());
        } catch (RuntimeException exception) {
            log.error("RSS import run {} failed", run.getId(), exception);
            try {
                history.fail(run, exception);
            } catch (RuntimeException historyException) {
                log.error("Unable to record failure for RSS import run {}", run.getId(), historyException);
            }
            throw new RssImportException(run.getId(), exception);
        }
    }

    private void processItems(IngestionRun run) {
        var items = rssParser.parse(rssClient.fetch());
        run.setTotalItems(items.size());
        for (var item : items) {
            var article = normalizer.normalize(item);
            if (article.isEmpty()) {
                run.setInvalidCount(run.getInvalidCount() + 1);
                continue;
            }
            try {
                if (writer.insertIfAbsent(article.get())) {
                    run.setInsertedCount(run.getInsertedCount() + 1);
                } else {
                    run.setDuplicateCount(run.getDuplicateCount() + 1);
                }
            } catch (DataAccessException exception) {
                run.setFailedCount(run.getFailedCount() + 1);
                log.error("Unable to persist article in RSS import run {}", run.getId(), exception);
            }
        }
    }
}
