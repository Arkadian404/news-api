package org.zafu.news.dto.response;

import java.util.List;

public record ApiError(String field, List<String> messages) {
}
