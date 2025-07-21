package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.constants.enums.InstallmentStatus;
import com.hsf.hsfproject.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, UUID> {
    java.util.List<Installment> findByInstallmentPlan_Order_User_IdAndStatus(UUID userId, InstallmentStatus status);
}

