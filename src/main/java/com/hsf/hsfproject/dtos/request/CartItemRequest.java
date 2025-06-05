package com.hsf.hsfproject.dtos.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemRequest {
    Long cartId;
    Long productId;
    Integer quantity;
}
