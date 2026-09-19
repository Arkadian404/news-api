package org.zafu.news.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zafu.news.model.Article;
import org.zafu.news.repository.ArticleRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DevArticleSeeder implements ApplicationRunner {
    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        var categories = List.of("technology", "business", "science", "sports", "world");
        var today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        for (int index = 1; index <= 25; index++) {
            String sourceUrl = "https://example.com/dev/articles/" + index;
            if (articleRepository.existsBySourceUrl(sourceUrl)) {
                continue;
            }
            var article = new Article();
            article.setTitle("Demo article " + index);
            article.setSource("Demo News");
            article.setSourceUrl(sourceUrl);
            article.setCategory(categories.get((index - 1) % categories.size()));
            article.setDescription("Sample content for local development, article " + index + ".");
            article.setPublishedAt(today.minusDays((index - 1) / 5).atTime(9, index)
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
            articleRepository.save(article);
        }
    }
}
