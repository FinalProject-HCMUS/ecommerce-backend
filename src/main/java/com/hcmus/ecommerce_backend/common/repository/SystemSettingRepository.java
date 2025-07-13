package com.hcmus.ecommerce_backend.common.repository;

import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    Optional<SystemSetting> findByKey(String key);
    
    List<SystemSetting> findByServiceName(String serviceName);

    @Query(value = "SELECT DISTINCT s.service_name FROM system_settings s WHERE s.service_name IS NOT NULL", nativeQuery = true)
    List<String> findDistinctServiceNames();
}