package org.zafu.news.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(boolean success, int status, String message, T data, List<ApiError> errors) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 200, "Success", data, List.of());
    }

    public static ApiResponse<Void> failure(int status, String message, List<ApiError> errors) {
        return new ApiResponse<>(false, status, message, null, List.copyOf(errors));
    }
}
