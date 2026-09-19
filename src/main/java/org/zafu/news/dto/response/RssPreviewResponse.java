package org.zafu.news.dto.response;

public record RssPreviewResponse(String feedUrl, int byteCount, String xmlPreview) {
}
