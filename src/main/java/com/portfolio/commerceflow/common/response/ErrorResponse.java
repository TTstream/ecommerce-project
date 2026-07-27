package com.portfolio.commerceflow.common.response;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorResponse> fieldErrors
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public static ErrorResponse of(String code, String message, List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(code, message, fieldErrors);
    }

    public record FieldErrorResponse(
            String field,
            String message
    ) {
    }
}
