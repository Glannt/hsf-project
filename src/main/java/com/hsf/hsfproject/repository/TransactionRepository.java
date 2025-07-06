package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByOrderId(UUID orderId);
    
    @Query("SELECT t FROM Transaction t JOIN FETCH t.order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findByOrderUserId(@Param("userId") UUID userId);
    
    @Query("SELECT t FROM Transaction t JOIN FETCH t.order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId")
    Page<Transaction> findByOrderUserId(@Param("userId") UUID userId, Pageable pageable);
    
    List<Transaction> findByStatus(String status);
    Optional<Transaction> findByTransactionRef(String transactionRef);
}
