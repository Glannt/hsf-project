package com.hsf.hsfproject.constants;

public enum TransactionStatus {
    PENDING("PENDING", "Payment Pending"),
    PROCESSING("PROCESSING", "Payment Processing"),
    COMPLETED("COMPLETED", "Payment Completed"),
    FAILED("FAILED", "Payment Failed"),
    CANCELLED("CANCELLED", "Payment Cancelled"),
    REFUNDED("REFUNDED", "Payment Refunded");

    private final String code;
    private final String description;

    TransactionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionStatus fromCode(String code) {
        for (TransactionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown transaction status code: " + code);
    }
}