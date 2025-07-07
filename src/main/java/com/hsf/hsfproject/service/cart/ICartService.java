package com.hsf.hsfproject.service.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.CartItem;
import com.hsf.hsfproject.model.User;

import java.util.List;
import java.util.UUID;

public interface ICartService {
    Cart createCart(User user);
    CartItem addCartItemToCart(CartItemRequest cartItemRequest);
    void updateCartItem(String id, int newQuantity);
    Cart getCartByUserId(String userId);
    void removeCartItemById(String cartItemId);
    void removeFromCart(String username, UUID itemId);
    void updateQuantity(String username, UUID itemId, int quantity);
}
