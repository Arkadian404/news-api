package org.zafu.news.exception;

public class RssImportInProgressException extends RuntimeException {
    public RssImportInProgressException() {
        super("An RSS import is already running; try again after it finishes");
    }
}
