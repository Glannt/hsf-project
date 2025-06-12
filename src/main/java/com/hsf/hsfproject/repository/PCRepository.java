package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.PC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PCRepository extends JpaRepository<PC, UUID> {
    // Define any custom query methods if needed
    // For example:
    // List<PC> findByBrand(String brand);
//    List<PC> findTop5ByOrderByTotalPriceDesc();
    PC findByName(String name);
}