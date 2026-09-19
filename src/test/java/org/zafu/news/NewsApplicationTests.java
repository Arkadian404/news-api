package org.zafu.news;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.flywaydb.core.Flyway;
import org.zafu.news.config.DevArticleSeeder;
import org.zafu.news.model.Article;
import org.zafu.news.repository.ArticleRepository;
import org.zafu.news.service.ArticleService;

import java.sql.DriverManager;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "app.seed.enabled=false")
class NewsApplicationTests {
    private static final PostgreSQLContainer DATABASE = new PostgreSQLContainer("postgres:18.0");

    static {
        DATABASE.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
    }

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleService articleService;

    @BeforeEach
    void clearArticles() {
        articleRepository.deleteAllInBatch();
    }

    @Test
    void contextLoads() {
        assertThat(articleRepository.count()).isZero();
    }

    @Test
    void filtersVietnameseDayInclusivelyAndCombinesCategory() {
        saveArticle("Before", "science", "2026-09-09T16:59:59Z", null);
        var start = saveArticle("Start", "science", "2026-09-09T17:00:00Z", null);
        var end = saveArticle("End", "science", "2026-09-10T16:59:59Z", null);
        saveArticle("After", "science", "2026-09-10T17:00:00Z", null);
        saveArticle("Other", "sports", "2026-09-10T10:00:00Z", null);
        saveArticle("Unknown date", "science", null, null);
        LocalDate day = LocalDate.of(2026, 9, 10);

        var result = articleService.getArticles(0, 20, " science ", day, day);

        assertThat(result.items()).extracting("id").containsExactly(end.getId(), start.getId());
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(articleService.getArticles(0, 20, null, day, null).totalElements()).isEqualTo(4);
        assertThat(articleService.getArticles(0, 20, null, null, day).totalElements()).isEqualTo(4);
    }

    @Test
    void ordersNullDatesLastBreaksTiesByIdAndPaginates() {
        var old = saveArticle("Old", "science", "2026-09-09T00:00:00Z", null);
        var first = saveArticle("First", "science", "2026-09-10T00:00:00Z", null);
        var second = saveArticle("Second", "science", "2026-09-10T00:00:00Z", null);
        var unknown = saveArticle("Unknown", "science", null, null);

        var firstPage = articleService.getArticles(0, 2, "  ", null, null);
        var secondPage = articleService.getArticles(1, 2, null, null, null);

        assertThat(firstPage.items()).extracting("id").containsExactly(second.getId(), first.getId());
        assertThat(firstPage.totalElements()).isEqualTo(4);
        assertThat(firstPage.totalPages()).isEqualTo(2);
        assertThat(secondPage.items()).extracting("id").containsExactly(old.getId(), unknown.getId());
        assertThat(articleService.getArticles(10, 2, null, null, null).items()).isEmpty();
    }

    @Test
    void mapsDetailsAndManagesTimestamps() {
        var article = saveArticle("Article", "science", "2026-09-10T00:00:00Z", "https://example.com/real");
        var original = articleService.getArticleById(article.getId());
        article.setTitle("Updated");
        articleRepository.saveAndFlush(article);
        var updated = articleService.getArticleById(article.getId());

        assertThat(original.source()).isEqualTo("Test News");
        assertThat(original.description()).isEqualTo("Test description");
        assertThat(original.createdAt()).isNotNull();
        assertThat(updated.createdAt()).isEqualTo(original.createdAt());
        assertThat(updated.updatedAt()).isAfterOrEqualTo(original.updatedAt());
        assertThat(updated.title()).isEqualTo("Updated");
    }

    @Test
    void databaseRejectsDuplicateSourceUrls() {
        saveArticle("First", "science", null, "https://example.com/duplicate");
        assertThatThrownBy(() -> saveArticle("Second", "science", null, "https://example.com/duplicate"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void seedIsRepeatableAndPreservesExistingData() {
        var existing = saveArticle("Keep me", "custom", null, "https://example.com/dev/articles/1");
        var seeder = new DevArticleSeeder(articleRepository);
        seeder.run(new DefaultApplicationArguments());
        seeder.run(new DefaultApplicationArguments());

        assertThat(articleRepository.count()).isEqualTo(25);
        assertThat(articleRepository.findById(existing.getId()).orElseThrow().getTitle()).isEqualTo("Keep me");
        assertThat(articleService.getArticles(0, 100, null, null, null).items())
                .extracting("category").contains("technology", "business", "science", "sports", "world");
    }

    @Test
    void migrationUpgradesV1WithoutInventingArticleMetadata() throws Exception {
        var configuration = Flyway.configure().dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(),
                DATABASE.getPassword()).schemas("legacy_upgrade").locations("classpath:db/migration");
        configuration.target("1").load().migrate();
        try (var connection = DriverManager.getConnection(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO legacy_upgrade.articles (title) VALUES ('Legacy article')");
            configuration.target("latest").load().migrate();
            try (var rows = statement.executeQuery("SELECT * FROM legacy_upgrade.articles")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("title")).isEqualTo("Legacy article");
                assertThat(rows.getString("source_url")).isNull();
                assertThat(rows.getTimestamp("published_at")).isNull();
                assertThat(rows.getTimestamp("created_at")).isNotNull();
                assertThat(rows.getTimestamp("updated_at")).isNotNull();
                assertThat(rows.next()).isFalse();
            }
        }
    }

    private Article saveArticle(String title, String category, String publishedAt, String sourceUrl) {
        var article = new Article();
        article.setTitle(title);
        article.setSource("Test News");
        article.setCategory(category);
        article.setDescription("Test description");
        article.setSourceUrl(sourceUrl);
        article.setPublishedAt(publishedAt == null ? null : Instant.parse(publishedAt));
        return articleRepository.saveAndFlush(article);
    }

}
