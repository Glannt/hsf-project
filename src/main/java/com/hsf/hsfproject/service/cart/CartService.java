package com.hsf.hsfproject.service.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.CartItem;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.CartItemRepository;
import com.hsf.hsfproject.repository.CartRepository;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.PCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService{
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PCRepository pcRepository;
    private final ComputerItemRepository computerItemRepository;
    // Implement the methods defined in ICartService
    @Override
    public Cart createCart(User user) {
        Cart cart = Cart.builder()
                .totalPrice(0.0)
                .itemCount(0)
                .user(user)
                .build();
        cartRepository.save(cart);

        return cart;
    }

    @Override
    public CartItem addCartItemToCart(CartItemRequest cartItemRequest) {
        // Retrieve the cart using the cart ID from the request
        Cart cart = cartRepository.findById(cartItemRequest.getCartId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with ID: " + cartItemRequest.getCartId()));

        CartItem cartItem = new CartItem();
        // Create a new CartItem from the request
       if(pcRepository.existsById(cartItemRequest.getProductId())) {
             cartItem = CartItem.builder()
                    .cart(cart)
                    .pc(pcRepository.findById(cartItemRequest.getProductId()).orElse(null))
                    .quantity(cartItemRequest.getQuantity())
                    .build();

            // Save the CartItem to the repository
            cartItemRepository.save(cartItem);

            // Update the cart's total price and item count
            double itemPrice = cartItem.getPc().getTotalPrice() * cartItem.getQuantity();
            cart.setTotalPrice(cart.getTotalPrice() + itemPrice);
            cart.setItemCount(cart.getItemCount() + 1);
            cartRepository.save(cart);

        }
        if(computerItemRepository.existsById(cartItemRequest.getProductId())) {
             cartItem = CartItem.builder()
                    .cart(cart)
                    .computerItem(computerItemRepository.findById(cartItemRequest.getProductId()).orElse(null))
                    .quantity(cartItemRequest.getQuantity())
                    .build();

            // Save the CartItem to the repository
            cartItemRepository.save(cartItem);

            // Update the cart's total price and item count
            double itemPrice = cartItem.getComputerItem().getPrice() * cartItem.getQuantity();
            cart.setTotalPrice(cart.getTotalPrice() + itemPrice);
            cart.setItemCount(cart.getItemCount() + 1);
            cartRepository.save(cart);


        }
        return cartItem;
    }
}
