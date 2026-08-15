package com.portfolio.commerceflow.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.commerceflow.common.exception.ErrorCode;
import com.portfolio.commerceflow.common.response.ApiResponse;
import com.portfolio.commerceflow.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.failure(ErrorResponse.of(errorCode.getCode(), errorCode.getMessage()))
        );
    }
}
