package com.hcmus.ecommerce_backend.common.model.mapper;

import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.SettingDataType;
import com.hcmus.ecommerce_backend.common.model.dto.SystemSettingResponse;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemSettingMapper {
    default SystemSettingResponse toResponse(SystemSetting systemSetting) {
        Object typedValue = castValue(systemSetting.getValue(), systemSetting.getType());
        return new SystemSettingResponse(
                systemSetting.getId(),
                systemSetting.getKey(),
                typedValue,
                systemSetting.getServiceName()
        );
    }

    private Object castValue(String value, SettingDataType type) {
        switch (type) {
            case String:
                return value;
            case JSON:
                return parseJson(value);
            case Encrypted:
                return decryptValue(value);
            case Double:
                return Double.parseDouble(value);
            default:
                throw new IllegalArgumentException("Unsupported data type: " + type);
        }
    }

    private Object parseJson(String value) {
        // Implement JSON parsing logic here (e.g., using Jackson or Gson)
        return value; // Placeholder
    }

    private Object decryptValue(String value) {
        // Implement decryption logic here if needed
        return value; // Placeholder
    }
}