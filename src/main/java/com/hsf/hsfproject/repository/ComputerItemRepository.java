package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.ComputerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComputerItemRepository extends JpaRepository<ComputerItem, UUID> {
//    List<ComputerItem> findByCategory(String category);
    ComputerItem findByName(String name);
    List<ComputerItem> findByCategoryId(UUID categoryId);
}

