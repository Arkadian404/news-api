package org.zafu.news.mapper;

import org.mapstruct.Mapper;
import org.zafu.news.dto.response.ArticleResponse;
import org.zafu.news.model.Article;

@Mapper(componentModel = "spring")
public interface ArticleMapper {
    ArticleResponse toResponse(Article article);
}
