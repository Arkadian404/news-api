package org.zafu.news.exception;

import lombok.Getter;

@Getter
public class InvalidArticleQueryException extends RuntimeException {
    private final String field;

    public InvalidArticleQueryException(String field, String message) {
        super(message);
        this.field = field;
    }
}
