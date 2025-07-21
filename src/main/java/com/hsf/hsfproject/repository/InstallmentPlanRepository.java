package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.InstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstallmentPlanRepository extends JpaRepository<InstallmentPlan, UUID> {
    List<InstallmentPlan> findByOrder_User_Id(UUID userId);
}
