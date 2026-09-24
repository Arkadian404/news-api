package org.zafu.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zafu.news.model.IngestionRun;

import java.time.Instant;

public interface IngestionRunRepository extends JpaRepository<IngestionRun, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE ingestion_runs
            SET run_status = 'FAILED', finished_at = :finishedAt,
                error_code = 'INTERRUPTED',
                error_message = 'Previous import did not finalize before application restart'
            WHERE run_status = 'RUNNING'
            """, nativeQuery = true)
    int markInterruptedRuns(@Param("finishedAt") Instant finishedAt);
}
