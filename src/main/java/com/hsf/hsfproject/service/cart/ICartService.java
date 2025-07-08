package com.hsf.hsfproject.service.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.CartItem;
import com.hsf.hsfproject.model.User;

import java.util.List;

public interface ICartService {
    Cart createCart(User user);
    CartItem addCartItemToCart(CartItemRequest cartItemRequest);
    void updateCartItem(String id, int newQuantity);
    Cart getCartByUserId(String userId);
    void removeCartItemById(String cartItemId);
}
