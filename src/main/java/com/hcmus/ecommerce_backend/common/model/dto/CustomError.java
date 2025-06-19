package com.hcmus.ecommerce_backend.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CustomError {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private HttpStatus httpStatus;

    private String header;

    private String exceptionName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @Builder.Default
    private final Boolean isSuccess = false;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CustomSubError> subErrors;

    @Getter
    @Builder
    public static class CustomSubError {
        private String message;
        private String field;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Object value;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String type;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Header {
        API_ERROR("API ERROR"),
        ALREADY_EXIST("ALREADY EXISTS"),
        NOT_FOUND("NOT FOUND"),
        VALIDATION_ERROR("VALIDATION ERROR"),
        DATABASE_ERROR("DATABASE ERROR"),
        PROCESS_ERROR("PROCESS ERROR"),
        AUTH_ERROR("AUTHENTICATION ERROR");

        private final String name;
    }
}