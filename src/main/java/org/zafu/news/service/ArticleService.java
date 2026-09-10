package org.zafu.news.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zafu.news.dto.response.ArticleResponse;
import org.zafu.news.mapper.ArticleMapper;
import org.zafu.news.model.Article;
import org.zafu.news.repository.ArticleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new RuntimeException("Article not found"));
        return articleMapper.toResponse(article);
    }

    public Page<ArticleResponse> getArticles(Pageable pageable) {
        return articleRepository.findAll(pageable).map(articleMapper::toResponse);
    }
}
