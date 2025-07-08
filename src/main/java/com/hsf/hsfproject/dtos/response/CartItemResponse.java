package com.hsf.hsfproject.dtos.response;

import com.hsf.hsfproject.model.CartItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemResponse {
    private CartItem product;
    private int quantity;
}
