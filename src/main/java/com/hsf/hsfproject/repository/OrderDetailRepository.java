package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {
    // Additional query methods can be defined here if needed

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderDetail od WHERE od.pc.id = :pcId")
    void deleteByPcId(UUID pcId);
}