package com.hcmus.ecommerce_backend.message.model.enums;

public enum RoleChat {
    USER("user"),
    ADMIN("admin"),
    SYSTEM("system");

    private final String value;

    RoleChat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleChat fromValue(String value) {
        for (RoleChat role : RoleChat.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
