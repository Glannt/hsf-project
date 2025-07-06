package com.hsf.hsfproject.constants;

public enum OrderStatus {
    PENDING("PENDING", "Order Pending"),
    CONFIRMED("CONFIRMED", "Order Confirmed"),
    PROCESSING("PROCESSING", "Order Processing"),
    SHIPPED("SHIPPED", "Order Shipped"),
    DELIVERED("DELIVERED", "Order Delivered"),
    CANCELLED("CANCELLED", "Order Cancelled"),
    RETURNED("RETURNED", "Order Returned");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}