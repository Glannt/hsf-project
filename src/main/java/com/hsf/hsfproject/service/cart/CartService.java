package com.hsf.hsfproject.service.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.*;
import com.hsf.hsfproject.repository.CartItemRepository;
import com.hsf.hsfproject.repository.CartRepository;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.PCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
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
        Cart cart = cartRepository.findById(UUID.fromString(request.getCartId()))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        Optional<PC> pcOpt = pcRepository.findById(UUID.fromString(request.getProductId()));
        if (pcOpt.isPresent()) {
            Optional<CartItem> existingPCOpt = cartItemRepository.findByCartIdAndPcId(cart.getId(), pcOpt.get().getId());
            if( existingPCOpt.isPresent()) {
                CartItem existingPC = existingPCOpt.get();
                existingPC.setQuantity(existingPC.getQuantity() + request.getQuantity());
                existingPC.setSubtotal(existingPC.getUnitPrice() * existingPC.getQuantity());
                cartItemRepository.save(existingPC);
                updateCart(cart, pcOpt.get().getPrice() * request.getQuantity());
                return existingPC;
            }
            return addPCToCart(cart, pcOpt.get(), request.getQuantity());
        }

        Optional<ComputerItem> itemOpt = computerItemRepository.findById(UUID.fromString(request.getProductId()));
        if (itemOpt.isPresent()) {
            Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndComputerItemId(cart.getId(), itemOpt.get().getId());
            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                existingItem.setSubtotal(existingItem.getUnitPrice() * existingItem.getQuantity());
                cartItemRepository.save(existingItem);
                updateCart(cart, itemOpt.get().getPrice() * request.getQuantity());
                return existingItem;
            }
            return addComputerItemToCart(cart, itemOpt.get(), request.getQuantity());
        }

        throw new IllegalArgumentException("Product not found");
    }

    @Override
    public void updateCartItem(String id, int newQuantity) {
        CartItem cartItem = cartItemRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        double oldPrice = 0.0;
        oldPrice = cartItem.getUnitPrice() * cartItem.getQuantity();
        cartItem.setQuantity(newQuantity);
        double newPrice = cartItem.getUnitPrice() * newQuantity;
        updateCart(cartItem.getCart(), newPrice - oldPrice);


        cartItemRepository.save(cartItem);

    }

    @Override
    public Cart getCartByUserId(String userId) {
        Cart cart = cartRepository.findByUserIdWithItems(UUID.fromString(userId));
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for user ID: " + userId);
        }
        return cart;


    }

    @Override
    public void removeCartItemById(String cartItemId) {
        CartItem cartItem = cartItemRepository.findById(UUID.fromString(cartItemId))
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        Cart cart = cartItem.getCart();
        double priceToRemove = cartItem.getUnitPrice() * cartItem.getQuantity();

        cartItemRepository.delete(cartItem);
        updateCart(cart, -priceToRemove);
    }

    private CartItem addPCToCart(Cart cart, PC pc, int quantity) {
        CartItem cartItem = CartItem.builder()
                .productName(pc.getName())
                .cart(cart)
                .pc(pc)
                .unitPrice(pc.getPrice())
                .quantity(quantity)
                .subtotal(pc.getPrice() * quantity)
                .build();

        cartItemRepository.save(cartItem);
        double price = pc.getPrice() * quantity;
        updateCart(cart, price);
        return cartItem;
    }

    private CartItem addComputerItemToCart(Cart cart, ComputerItem item, int quantity) {
        CartItem cartItem = CartItem.builder()
                .productName(item.getName())
                .cart(cart)
                .computerItem(item)
                .unitPrice(item.getPrice())
                .quantity(quantity)
                .subtotal(item.getPrice() * quantity)
                .build();

        cartItemRepository.save(cartItem);
        double price = item.getPrice() * quantity;
        updateCart(cart, price);
        return cartItem;
    }

    private void updateCart(Cart cart, double addedPrice) {
        cart.setTotalPrice(cart.getTotalPrice() + addedPrice);
        Long count = cartItemRepository.countCartItemsByCartId(cart.getId());
        cart.setItemCount(count.intValue());
        cartRepository.save(cart);
    }

}
