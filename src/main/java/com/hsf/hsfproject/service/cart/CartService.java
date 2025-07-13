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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Iterator;

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
                updateCart(cart, 0.0); // Recalculate total
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
                updateCart(cart, 0.0); // Recalculate total
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

        cartItem.setQuantity(newQuantity);
        cartItem.setSubtotal(cartItem.getUnitPrice() * newQuantity);
        cartItemRepository.save(cartItem);
        
        // Recalculate cart total
        updateCart(cartItem.getCart(), 0.0); // 0.0 is ignored, total is recalculated
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
        cartItemRepository.delete(cartItem);
        updateCart(cart, 0.0); // Recalculate total
    }
    
    @Override
    @Transactional
    public void removeFromCart(String username, UUID itemId) {
        System.out.println("===> Service: removeFromCart, username: " + username + ", itemId: " + itemId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        // Verify the cart item belongs to the user
        if (!cartItem.getCart().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Cart item does not belong to user");
        }
        Cart cart = cartItem.getCart();
        
        // Remove from database first
        cartItemRepository.delete(cartItem);
        
        // Then remove from cart's set
        if (cart.getCartItems() != null) {
            cart.getCartItems().removeIf(item -> item.getId().equals(itemId));
        }
        
        // Save cart and recalculate total
        cartRepository.save(cart);
        updateCart(cart, 0.0); // Recalculate total
    }
    
    @Override
    public void updateQuantity(String username, UUID itemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        // Verify the cart item belongs to the user
        if (!cartItem.getCart().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Cart item does not belong to user");
        }
        if (quantity <= 0) {
            // Remove item if quantity is zero or less
            Cart cart = cartItem.getCart();
            if (cart.getCartItems() != null) {
                cart.getCartItems().remove(cartItem);
            }
            cartItem.setCart(null);
            cartItemRepository.delete(cartItem);
            updateCart(cart, 0.0); // Recalculate total
            cartRepository.save(cart);
            return;
        }
        cartItem.setQuantity(quantity);
        cartItem.setSubtotal(cartItem.getUnitPrice() * quantity);
        cartItemRepository.save(cartItem);
        updateCart(cartItem.getCart(), 0.0); // Recalculate total
    }

    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setTotalPrice(0.0);
            cart.setItemCount(0);
            cartRepository.save(cart);
        }
    }

    public Cart getCartById(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with ID: " + cartId));
    }
    
    /**
     * Recalculate cart total price from all cart items
     * This method can be used to fix cart total inconsistencies
     */
    public void recalculateCartTotal(UUID cartId) {
        Cart cart = getCartById(cartId);
        updateCart(cart, 0.0); // 0.0 is ignored, total is recalculated
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
        updateCart(cart, 0.0); // Recalculate total
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
        updateCart(cart, 0.0); // Recalculate total
        return cartItem;
    }

    private void updateCart(Cart cart, double addedPrice) {
        // Recalculate total price from database to ensure accuracy
        double totalPrice = 0.0;
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                totalPrice += item.getUnitPrice() * item.getQuantity();
            }
        }
        cart.setTotalPrice(totalPrice);
        
        Long count = cartItemRepository.countCartItemsByCartId(cart.getId());
        cart.setItemCount(count.intValue());
        cartRepository.save(cart);
    }

}
