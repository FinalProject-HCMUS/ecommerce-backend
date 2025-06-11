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

    @Query("SELECT DISTINCT s.serviceName FROM SystemSetting s")
    List<String> findDistinctServiceNames();
}