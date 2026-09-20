package org.zafu.news.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zafu.news.service.IngestionRunService;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class RssImportStartupRecovery {
    private final IngestionRunService history;

    @PostConstruct
    public void recover() {
        int recovered = history.recoverInterruptedRuns();
        log.info("Marked {} unfinished RSS imports as interrupted", recovered);
    }
}
