package com.hcmus.ecommerce_backend.common.service;

import java.util.List;

import com.hcmus.ecommerce_backend.common.model.dto.SystemSettingResponse;
import com.hcmus.ecommerce_backend.common.model.dto.UpdateSystemSettingRequest;

public interface SystemSettingService {
    List<SystemSettingResponse> getAllSystemSettings(String serviceName);
    List<SystemSettingResponse> updateSystemSettings(UpdateSystemSettingRequest request);
}
