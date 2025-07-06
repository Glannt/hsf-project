package com.hsf.hsfproject.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VnPayRequest {
    private long amount;
    private String language;
    private String orderId;
    private String orderInfo;
    private String returnUrl;
    private String userId;
    private String shippingAddress;
}
