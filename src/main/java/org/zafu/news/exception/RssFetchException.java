package org.zafu.news.exception;

public class RssFetchException extends RuntimeException {
    public RssFetchException(String message) {
        super(message);
    }

    public RssFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
