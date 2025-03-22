package com.hcmus.ecommerce_backend.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Standard response wrapper for all API responses")
public class CustomResponse<T> {

    @Builder.Default
    @Schema(description = "Timestamp of the response", example = "2023-01-01T12:00:00")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "HTTP status of the response", example = "OK")
    private HttpStatus httpStatus;

    @Schema(description = "Indicates if the operation was successful", example = "true")
    private Boolean isSuccess;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Response data (null for operations without return data)")
    private T data;

    @Schema(hidden = true)
    public static final CustomResponse<Void> SUCCESS = CustomResponse.<Void>builder()
            .httpStatus(HttpStatus.OK)
            .isSuccess(true)
            .build();

    @Schema(hidden = true)
    public static <T> CustomResponse<T> successOf(final T data) {
        return CustomResponse.<T>builder()
                .httpStatus(HttpStatus.OK)
                .isSuccess(true)
                .data(data)
                .build();
    }

    @Schema(hidden = true)
    public static <T> CustomResponse<T> createdOf(final T data) {
        return CustomResponse.<T>builder()
                .httpStatus(HttpStatus.CREATED)
                .isSuccess(true)
                .data(data)
                .build();
    }
}