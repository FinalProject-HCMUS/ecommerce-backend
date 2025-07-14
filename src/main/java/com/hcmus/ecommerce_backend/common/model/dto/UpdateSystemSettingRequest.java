package com.hcmus.ecommerce_backend.common.model.dto;

import java.util.List;

import com.google.auto.value.AutoValue.Builder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Builder
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update multiple system settings based on key-value pairs")
public class UpdateSystemSettingRequest {

    @Schema(description = "List of key-value pairs to update")
    private List<KeyValueUpdate> updates;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Key-value pair for updating a system setting")
    public static class KeyValueUpdate {
        @Schema(description = "Key of the system setting", example = "STORAGE_TYPE")
        private String key;

        @Schema(description = "New value for the system setting", example = "AWS_S3")
        private String value;
    }
}
