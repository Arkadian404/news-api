package org.zafu.news.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.zafu.news.dto.response.ApiResponse;
import org.zafu.news.dto.response.ApiError;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(RssTimeoutException.class)
    public ResponseEntity<Object> handleRssTimeout(RssTimeoutException exception, WebRequest request) {
        return failure(exception, HttpStatus.GATEWAY_TIMEOUT, exception.getMessage(), request);
    }

    @ExceptionHandler(RssFetchException.class)
    public ResponseEntity<Object> handleRssFailure(RssFetchException exception, WebRequest request) {
        return failure(exception, HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ArticleNotFoundException exception, WebRequest request) {
        return failure(exception, HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidArticleQueryException.class)
    public ResponseEntity<Object> handleInvalidQuery(InvalidArticleQueryException exception, WebRequest request) {
        var body = ApiResponse.failure(400, "Invalid article query",
                List.of(new ApiError(exception.getField(), List.of(exception.getMessage()))));
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var errors = exception.getParameterValidationResults().stream().map(result -> new ApiError(
                String.valueOf(result.getMethodParameter().getParameterName()),
                result.getResolvableErrors().stream()
                        .map(error -> String.valueOf(error.getDefaultMessage())).toList())).toList();
        var body = ApiResponse.failure(status.value(), "Invalid request parameters", errors);
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(org.springframework.beans.TypeMismatchException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiError> errors = List.of();
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            errors = List.of(new ApiError(mismatch.getName(), List.of("Invalid value or format")));
        }
        var body = ApiResponse.failure(status.value(), "Invalid request parameter format", errors);
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception exception, WebRequest request) {
        log.error("Unexpected request failure", exception);
        return failure(exception, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<Object> failure(Exception exception, HttpStatus status, String message, WebRequest request) {
        return handleExceptionInternal(exception, ApiResponse.failure(status.value(), message, List.of()),
                new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (body instanceof ProblemDetail problemDetail) {
            String message = status.is5xxServerError() ? "An unexpected error occurred" : problemDetail.getDetail();
            body = ApiResponse.failure(status.value(), message == null ? problemDetail.getTitle() : message, List.of());
        }
        var responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return super.createResponseEntity(body, responseHeaders, status, request);
    }
}
