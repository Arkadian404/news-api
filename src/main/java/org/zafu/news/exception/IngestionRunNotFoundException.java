package org.zafu.news.exception;

public class IngestionRunNotFoundException extends RuntimeException {
    public IngestionRunNotFoundException(Long id) {
        super("Ingestion run " + id + " not found");
    }
}
