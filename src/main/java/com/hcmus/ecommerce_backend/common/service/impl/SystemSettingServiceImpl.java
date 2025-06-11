package com.hcmus.ecommerce_backend.common.service.impl;

import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.dto.UpdateSystemSettingRequest;
import com.hcmus.ecommerce_backend.common.exception.KeyNotFoundException;
import com.hcmus.ecommerce_backend.common.model.dto.SystemSettingResponse;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.SystemSettingService;
import com.hcmus.ecommerce_backend.common.model.mapper.SystemSettingMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final SystemSettingMapper systemSettingMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getAllSystemSettings(String serviceName) {
        log.info("SystemSettingServiceImpl | getAllSystemSettings | Retrieving settings for service: {}", serviceName);
        List<SystemSetting> settings;
        if (serviceName != null && !serviceName.isEmpty()) {
            settings = systemSettingRepository.findByServiceName(serviceName);
        } else {
            settings = systemSettingRepository.findAll();
        }
        return settings.stream()
                .map(systemSettingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SystemSettingResponse> updateSystemSettings(UpdateSystemSettingRequest request) {
        log.info("SystemSettingServiceImpl | updateSystemSettings | Updating settings based on key-value pairs");

        // Lấy danh sách tất cả các SystemSetting từ database
        List<SystemSetting> settings = systemSettingRepository.findAll();

        // Kiểm tra và cập nhật các giá trị theo key-value từ request
        for (UpdateSystemSettingRequest.KeyValueUpdate update : request.getUpdates()) {
            boolean keyFound = settings.stream()
                    .filter(setting -> setting.getKey().equals(update.getKey()))
                    .findFirst()
                    .map(setting -> {
                        log.info("Updating key: {} with value: {}", update.getKey(), update.getValue());
                        setting.setValue(update.getValue());
                        return true;
                    })
                    .orElse(false);

            // Nếu key không được tìm thấy, ném ngoại lệ
            if (!keyFound) {
                log.error("SystemSettingServiceImpl | updateSystemSettings | Key not found: {}", update.getKey());
                throw new KeyNotFoundException(update.getKey());
            }
        }

        // Lưu các thay đổi
        List<SystemSetting> updatedSettings = systemSettingRepository.saveAll(settings);
        log.info("SystemSettingServiceImpl | updateSystemSettings | Updated settings based on key-value pairs");

        // Trả về danh sách đã cập nhật
        return updatedSettings.stream()
                .map(systemSettingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllServiceNames() {
        log.info("SystemSettingServiceImpl | getAllServiceNames | Retrieving all distinct service names");
        return systemSettingRepository.findDistinctServiceNames();
    }
}