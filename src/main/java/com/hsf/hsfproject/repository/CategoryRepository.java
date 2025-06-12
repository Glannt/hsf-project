package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // You can define custom query methods here if needed
    // For example, to find a category by its name:
    Category findByName(String name);
}

