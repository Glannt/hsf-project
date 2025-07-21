package com.hsf.hsfproject.service.installment;

import com.hsf.hsfproject.constants.enums.InstallmentStatus;
import com.hsf.hsfproject.model.Installment;
import com.hsf.hsfproject.model.InstallmentPlan;
import com.hsf.hsfproject.model.InstalllmentType;
import com.hsf.hsfproject.model.Order;

import java.util.List;
import java.util.UUID;

public interface InstallmentService {
    InstallmentPlan createInstallmentPlan(Order order, InstalllmentType installmentType);

    double calculateMonthlyAmount(double totalAmount, InstalllmentType installmentType);

    List<InstallmentPlan> getUserInstallmentPlans(String userId);

    Installment makeInstallmentPayment(UUID installmentId, double amount, String paymentMethod);

    Installment updateInstallmentStatus(UUID installmentId, InstallmentStatus status);
}