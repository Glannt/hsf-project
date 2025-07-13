package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :id")
    Long countCartItemsByCartId(UUID id);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.pc.id = :productId")
    Optional<CartItem> findByCartIdAndPcId(UUID cartId, UUID productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.computerItem.id = :productId")
    Optional<CartItem> findByCartIdAndComputerItemId(UUID cartId, UUID productId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartId(UUID cartId);
    
    void deleteByCartId(UUID cartId);
}
