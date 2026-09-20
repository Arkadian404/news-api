package org.zafu.news.exception;

public class RssImportException extends RuntimeException {
    public RssImportException(Long runId, RuntimeException cause) {
        super("RSS import run " + runId + " failed", cause);
    }
}
