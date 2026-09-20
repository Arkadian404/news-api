package org.zafu.news.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zafu.news.model.Article;

@Repository
@RequiredArgsConstructor
public class RssArticleWriter {
    private final ArticleRepository articleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean insertIfAbsent(Article article) {
        return articleRepository.insertIfAbsent(article) == 1;
    }
}
