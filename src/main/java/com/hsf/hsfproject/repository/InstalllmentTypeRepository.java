package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.InstalllmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstalllmentTypeRepository extends JpaRepository<InstalllmentType, UUID> {
}

