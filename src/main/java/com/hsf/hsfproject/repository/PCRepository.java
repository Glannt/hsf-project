package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.PC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PCRepository extends JpaRepository<PC, Long> {
    // Define any custom query methods if needed
    // For example:
    // List<PC> findByBrand(String brand);
}