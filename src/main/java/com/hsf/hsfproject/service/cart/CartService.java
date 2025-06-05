package com.hsf.hsfproject.service.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.*;
import com.hsf.hsfproject.repository.CartItemRepository;
import com.hsf.hsfproject.repository.CartRepository;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.PCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

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
    public CartItem addCartItemToCart(CartItemRequest request) {
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        Optional<PC> pcOpt = pcRepository.findById(request.getProductId());
        if (pcOpt.isPresent()) {
            return addPCToCart(cart, pcOpt.get(), request.getQuantity());
        }

        Optional<ComputerItem> itemOpt = computerItemRepository.findById(request.getProductId());
        if (itemOpt.isPresent()) {
            return addComputerItemToCart(cart, itemOpt.get(), request.getQuantity());
        }

        throw new IllegalArgumentException("Product not found");
    }

    private CartItem addPCToCart(Cart cart, PC pc, int quantity) {
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .pc(pc)
                .quantity(quantity)
                .build();

        cartItemRepository.save(cartItem);
        double price = pc.getTotalPrice() * quantity;
        updateCart(cart, price);
        return cartItem;
    }

    private CartItem addComputerItemToCart(Cart cart, ComputerItem item, int quantity) {
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .computerItem(item)
                .quantity(quantity)
                .build();

        cartItemRepository.save(cartItem);
        double price = item.getPrice() * quantity;
        updateCart(cart, price);
        return cartItem;
    }

    private void updateCart(Cart cart, double addedPrice) {
        cart.setTotalPrice(cart.getTotalPrice() + addedPrice);
        cart.setItemCount(cart.getItemCount() + 1);
        cartRepository.save(cart);
    }
}
