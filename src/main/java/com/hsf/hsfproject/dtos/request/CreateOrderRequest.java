package com.hsf.hsfproject.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> orderItems;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemRequest {
        private UUID productId;
        private UUID pcId;
        private UUID computerItemId;
        private int quantity;
        private String productType; // "PC" or "COMPUTER_ITEM"
    }
} 