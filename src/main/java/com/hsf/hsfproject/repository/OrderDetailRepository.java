package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {
    // Additional query methods can be defined here if needed
}