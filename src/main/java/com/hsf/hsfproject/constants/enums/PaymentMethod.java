package com.hsf.hsfproject.constants.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY("COD", "Cash on Delivery"),
    VNPAY("VNPAY", "VNPay Online Payment"),
    BANK_TRANSFER("BANK", "Direct Bank Transfer");

    private final String code;
    private final String description;

    PaymentMethod(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method code: " + code);
    }
}
