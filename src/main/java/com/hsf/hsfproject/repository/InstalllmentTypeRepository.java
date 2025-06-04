package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.InstalllmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstalllmentTypeRepository extends JpaRepository<InstalllmentType, Long> {
}

