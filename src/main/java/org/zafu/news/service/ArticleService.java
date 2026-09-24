package org.zafu.news.service;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.zafu.news.dto.response.ArticleDetailResponse;
import org.zafu.news.dto.response.ArticlePageResponse;
import org.zafu.news.exception.ArticleNotFoundException;
import org.zafu.news.mapper.ArticleMapper;
import org.zafu.news.model.Article;
import org.zafu.news.repository.ArticleRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {
    private static final ZoneId NEWS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    public ArticleDetailResponse getArticleById(Long id) {
        return articleMapper.toDetailResponse(articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id)));
    }

    public ArticlePageResponse getArticles(int page, int size, String category, LocalDate from, LocalDate to,
                                           String keyword) {
        Specification<Article> specification = (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                var title = builder.lower(builder.function("public.unaccent", String.class, root.get("title")));
                var search = builder.lower(builder.function("public.unaccent", String.class,
                        builder.literal(keyword.strip())));
                predicates.add(builder.greaterThan(builder.locate(title, search), 0));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(builder.equal(root.get("category"), category.trim()));
            }
            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("publishedAt"),
                        from.atStartOfDay(NEWS_ZONE).toInstant()));
            }
            if (to != null) {
                predicates.add(builder.lessThan(root.get("publishedAt"),
                        to.plusDays(1).atStartOfDay(NEWS_ZONE).toInstant()));
            }
            if (query != null && query.getResultType() != Long.class) {
                query.orderBy(builder.asc(builder.<Integer>selectCase()
                                .when(builder.isNull(root.get("publishedAt")), 1).otherwise(0)),
                        builder.desc(root.get("publishedAt")), builder.desc(root.get("id")));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        var result = articleRepository.findAll(specification, PageRequest.of(page, size));
        return new ArticlePageResponse(result.getContent().stream().map(articleMapper::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
