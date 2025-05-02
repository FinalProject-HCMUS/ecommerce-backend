package com.hcmus.ecommerce_backend.common.model.entity;

import com.hcmus.ecommerce_backend.common.model.enums.SettingDataType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "system_settings")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "key", nullable = false, unique = true)
    private String key;
    
    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "service_name")
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SettingDataType type;
    
    // Constants for system setting keys
}