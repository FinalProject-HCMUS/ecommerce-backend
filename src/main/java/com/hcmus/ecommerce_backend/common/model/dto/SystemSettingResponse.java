package com.hcmus.ecommerce_backend.common.model.dto;

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
@Schema(description = "Response object for system settings")
public class SystemSettingResponse {
    @Schema(description = "Unique identifier of the system setting", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Key of the system setting", example = "max_upload_size")
    private String key;

    @Schema(description = "Value of the system setting with its corresponding type", example = "10485760")
    private Object value;

    @Schema(description = "Name of the service associated with the setting", example = "file_storage_service")
    private String serviceName;
}