package org.zafu.news.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ingestion_runs")
@Getter
@Setter
@NoArgsConstructor
public class IngestionRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String source;
    @Column(nullable = false, columnDefinition = "text")
    private String feedUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IngestionStatus runStatus;
    @Column(nullable = false, updatable = false)
    private Instant startedAt;
    private Instant finishedAt;
    @Column(nullable = false)
    private int totalItems;
    @Column(nullable = false)
    private int insertedCount;
    @Column(nullable = false)
    private int duplicateCount;
    @Column(nullable = false)
    private int invalidCount;
    @Column(nullable = false)
    private int failedCount;
    @Column(length = 50)
    private String errorCode;
    private String errorMessage;
}
