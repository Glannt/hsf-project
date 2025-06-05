package com.hsf.hsfproject.service.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.CartItem;
import com.hsf.hsfproject.model.User;

public interface ICartService {
    Cart createCart(User user);
    CartItem addCartItemToCart(CartItemRequest cartItemRequest);
    CartItem updateCartItem(Long cartItemId, int newQuantity);
    void deleteCartItem(Long cartItemId);
}
