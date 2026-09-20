package org.zafu.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zafu.news.model.Article;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    boolean existsBySourceUrl(String sourceUrl);

    @Modifying
    @Query(value = """
            INSERT INTO articles (title, source, source_url, category, description,
                                  published_at, created_at, updated_at)
            VALUES (:#{#article.title}, :#{#article.source}, :#{#article.sourceUrl},
                    :#{#article.category}, :#{#article.description}, :#{#article.publishedAt},
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (source_url) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("article") Article article);
}
