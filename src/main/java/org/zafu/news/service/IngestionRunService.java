package org.zafu.news.service;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zafu.news.dto.response.IngestionRunPageResponse;
import org.zafu.news.dto.response.IngestionRunResponse;
import org.zafu.news.exception.*;
import org.zafu.news.model.IngestionRun;
import org.zafu.news.model.IngestionStatus;
import org.zafu.news.repository.IngestionRunRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngestionRunService {
    private final IngestionRunRepository repository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IngestionRun start(String feedUrl) {
        var run = new IngestionRun();
        run.setSource("vnexpress");
        run.setFeedUrl(feedUrl);
        run.setRunStatus(IngestionStatus.RUNNING);
        run.setStartedAt(Instant.now());
        repository.saveAndFlush(run);
        entityManager.detach(run);
        return run;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(IngestionRun run) {
        boolean partial = run.getInvalidCount() > 0 || run.getFailedCount() > 0;
        run.setRunStatus(partial ? IngestionStatus.PARTIAL : IngestionStatus.SUCCEEDED);
        if (partial) {
            run.setErrorCode("ITEM_ERRORS");
            run.setErrorMessage("Some items were invalid or could not be saved");
        }
        run.setFinishedAt(Instant.now());
        repository.saveAndFlush(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(IngestionRun run, RuntimeException failure) {
        String code = switch (failure) {
            case RssTimeoutException ignored -> "SOURCE_TIMEOUT";
            case RssFetchException ignored -> "SOURCE_FETCH_FAILED";
            case RssParseException ignored -> "SOURCE_PARSE_FAILED";
            default -> "INTERNAL_ERROR";
        };
        run.setRunStatus(IngestionStatus.FAILED);
        run.setFinishedAt(Instant.now());
        run.setErrorCode(code);
        run.setErrorMessage("Import could not complete; see server logs for details");
        repository.saveAndFlush(run);
    }

    public IngestionRunResponse getById(Long id) {
        return toResponse(repository.findById(id).orElseThrow(() -> new IngestionRunNotFoundException(id)));
    }

    public IngestionRunPageResponse getRuns(int page, int size) {
        var runs = repository.findAll(PageRequest.of(page, size, Sort.by("startedAt", "id").descending()));
        return new IngestionRunPageResponse(runs.getContent().stream().map(this::toResponse).toList(),
                runs.getNumber(), runs.getSize(), runs.getTotalElements(), runs.getTotalPages());
    }

    private IngestionRunResponse toResponse(IngestionRun run) {
        return new IngestionRunResponse(run.getId(), run.getSource(), run.getFeedUrl(), run.getRunStatus(),
                run.getStartedAt(), run.getFinishedAt(), run.getTotalItems(), run.getInsertedCount(),
                run.getDuplicateCount(), run.getInvalidCount(), run.getFailedCount(),
                run.getErrorCode(), run.getErrorMessage());
    }
}
