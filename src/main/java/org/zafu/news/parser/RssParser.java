package org.zafu.news.parser;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.zafu.news.dto.response.RssItemResponse;
import org.zafu.news.exception.RssParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class RssParser {
    public List<RssItemResponse> parse(byte[] content) {
        try (var stream = new ByteArrayInputStream(content)) {
            var input = new SyndFeedInput();
            input.setAllowDoctypes(false);
            var feed = input.build(new InputSource(stream));
            return feed.getEntries().stream().map(this::toResponse).toList();
        } catch (FeedException | IOException | IllegalArgumentException exception) {
            throw new RssParseException(exception);
        }
    }

    private RssItemResponse toResponse(SyndEntry entry) {
        return new RssItemResponse(entry.getTitle(), entry.getLink(),
                entry.getDescription() == null ? null : entry.getDescription().getValue(),
                entry.getPublishedDate() == null ? null : entry.getPublishedDate().toInstant());
    }
}
