package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    // Additional query methods can be defined here if needed
}