package com.hcmus.ecommerce_backend.order.model.enums;

public enum Status {
    NEW ("new"),
    CANCELLED ("cancelled"),
    PROCESSING ("processing"),
    PACKAGED ("packaged"),
    PICKED ("picked"),
    SHIPPING ("shipping"),
    DELIVERED ("delivered"),
    REFUNDED ("refunded");
    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
