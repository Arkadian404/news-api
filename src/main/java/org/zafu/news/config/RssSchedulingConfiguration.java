package org.zafu.news.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.zafu.news.service.RssImportService;
import org.zafu.news.service.RssImportScheduler;

@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "app.ingestion.scheduling.enabled", havingValue = "true")
@EnableScheduling
public class RssSchedulingConfiguration {
    @Bean
    @DependsOn("rssImportStartupRecovery")
    public RssImportScheduler rssImportScheduler(RssImportService importService) {
        return new RssImportScheduler(importService);
    }
}
