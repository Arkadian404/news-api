package org.zafu.news.exception;

public class ArticleNotFoundException extends RuntimeException {
    public ArticleNotFoundException(Long id) {
        super("Article " + id + " not found");
    }
}
