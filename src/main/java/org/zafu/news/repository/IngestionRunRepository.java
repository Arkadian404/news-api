package org.zafu.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zafu.news.model.IngestionRun;

public interface IngestionRunRepository extends JpaRepository<IngestionRun, Long> {
}
