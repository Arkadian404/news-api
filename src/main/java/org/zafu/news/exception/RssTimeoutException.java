package org.zafu.news.exception;

public class RssTimeoutException extends RssFetchException {
    public RssTimeoutException(Throwable cause) {
        super("RSS source timed out", cause);
    }
}
