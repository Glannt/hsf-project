package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.ComputerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputerItemRepository extends JpaRepository<ComputerItem, Long> {
}

