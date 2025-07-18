package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(UUID userId);
    Optional<Cart> findById(UUID id);
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.id = :userId")
    Cart findByUserIdWithItems(@Param("userId") UUID userId);
}

