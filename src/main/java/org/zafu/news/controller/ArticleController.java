package org.zafu.news.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.zafu.news.dto.response.ArticleDetailResponse;
import org.zafu.news.dto.response.ApiResponse;
import org.zafu.news.dto.response.ArticlePageResponse;
import org.zafu.news.exception.InvalidArticleQueryException;
import org.zafu.news.service.ArticleService;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailResponse> getArticle(@PathVariable @Positive Long id) {
        return ApiResponse.ok(articleService.getArticleById(id));
    }

    @GetMapping({"", "/"})
    public ApiResponse<ArticlePageResponse> getArticles(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) @Size(max = 100) String q) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidArticleQueryException("from", "must be on or before to");
        }
        if (to != null && to.equals(LocalDate.MAX)) {
            throw new InvalidArticleQueryException("to", "must be before the maximum supported date");
        }
        return ApiResponse.ok(articleService.getArticles(page, size, category, from, to, q));
    }
}
