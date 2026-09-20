package org.zafu.news.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.zafu.news.dto.response.RssItemResponse;
import org.zafu.news.model.Article;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

@Component
public class RssArticleNormalizer {
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_URL_LENGTH = 2048;

    public Optional<Article> normalize(RssItemResponse item) {
        String title = normalizeTitle(item.title());
        String sourceUrl = normalizeUrl(item.sourceUrl());
        if (title == null || sourceUrl == null) {
            return Optional.empty();
        }

        var article = new Article();
        article.setTitle(title);
        article.setSourceUrl(sourceUrl);
        article.setSource("vnexpress");
        article.setCategory("technology");
        article.setDescription(normalizeDescription(item.description()));
        article.setPublishedAt(item.publishedAt());
        return Optional.of(article);
    }

    private String normalizeTitle(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String title = value.strip();
        return title.codePointCount(0, title.length()) > MAX_TITLE_LENGTH || title.indexOf('\0') >= 0
                ? null : title;
    }

    private String normalizeUrl(String value) {
        if (value == null) {
            return null;
        }
        try {
            URI uri = URI.create(value.strip());
            if (!isAllowedUrl(uri)) {
                return null;
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            String sourceUrl = URI.create(scheme + "://vnexpress.net" + uri.getRawPath()
                    + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery())).toASCIIString();
            return sourceUrl.length() > MAX_URL_LENGTH ? null : sourceUrl;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isAllowedUrl(URI uri) {
        boolean https = "https".equalsIgnoreCase(uri.getScheme());
        boolean http = "http".equalsIgnoreCase(uri.getScheme());
        boolean defaultPort = uri.getPort() == -1 || uri.getPort() == (https ? 443 : 80);
        return (https || http) && "vnexpress.net".equalsIgnoreCase(uri.getHost())
                && uri.getRawUserInfo() == null && defaultPort;
    }

    private String normalizeDescription(String value) {
        if (value == null) {
            return null;
        }
        return value.isBlank() ? null : value.replace("\0", "");
    }
}
