package org.zafu.news.exception;

public class RssParseException extends RuntimeException {
    public RssParseException(Throwable cause) {
        super("Unable to parse RSS source", cause);
    }
}
