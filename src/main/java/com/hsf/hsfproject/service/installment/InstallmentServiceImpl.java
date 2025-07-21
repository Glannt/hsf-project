package com.hsf.hsfproject.service.installment;

import com.hsf.hsfproject.constants.enums.InstallmentStatus;
import com.hsf.hsfproject.model.Installment;
import com.hsf.hsfproject.model.InstallmentPlan;
import com.hsf.hsfproject.model.InstalllmentType;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.repository.InstallmentPlanRepository;
import com.hsf.hsfproject.repository.InstallmentRepository;
import com.hsf.hsfproject.repository.InstalllmentTypeRepository;
import com.hsf.hsfproject.repository.OrderRepository;
import com.hsf.hsfproject.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {

    private final InstallmentPlanRepository installmentPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final InstalllmentTypeRepository installmentTypeRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final InstallmentPaymentService installmentPaymentService;

    @Override
    @Transactional
    public InstallmentPlan createInstallmentPlan(Order order, InstalllmentType type) {
        if (order == null || type == null) throw new IllegalArgumentException("Invalid data");
        if (order.getTotalPrice() < 100)
            throw new IllegalArgumentException("Order not eligible for installments");
        if (type.getMonths() <= 0)
            throw new IllegalArgumentException("Invalid installment type");
        if (order.getInstallmentPlan() != null)
            throw new IllegalStateException("Order already has an installment plan");

        // reject if user has late installments
        if (!installmentRepository.findByInstallmentPlan_Order_User_IdAndStatus(order.getUser().getId(), InstallmentStatus.LATE).isEmpty()) {
            throw new IllegalStateException("User has overdue installments");
        }
        double monthly = calculateMonthlyAmount(order.getTotalPrice(), type);
        InstallmentPlan plan = InstallmentPlan.builder()
                .planName(type.getName())
                .startDate(new Date())
                .monthlyPayment(monthly)
                .order(order)
                .installmentType(type)
                .build();

        List<Installment> installments = new ArrayList<>();
        for (int i = 1; i <= type.getMonths(); i++) {
            Installment ins = Installment.builder()
                    .dueDate(LocalDate.now().plusMonths(i))
                    .amountDue(monthly)
                    .status(InstallmentStatus.PENDING)
                    .installmentPlan(plan)
                    .build();
            installments.add(ins);
        }
        plan.setInstallments(installments);
        installmentPlanRepository.save(plan);
        return plan;
    }

    @Override
    public double calculateMonthlyAmount(double totalAmount, InstalllmentType type) {
        double interest = type.getInterestRate() != null ? totalAmount * type.getInterestRate() : 0d;
        return (totalAmount + interest) / type.getMonths();
    }

    @Override
    public List<InstallmentPlan> getUserInstallmentPlans(String userId) {
        return installmentPlanRepository.findByOrder_User_Id(UUID.fromString(userId));
    }

    @Override
    @Transactional
    public Installment makeInstallmentPayment(UUID installmentId, double amount, String paymentMethod) {
        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("Installment not found"));
        if (installment.getTransaction() != null)
            throw new IllegalStateException("Installment already paid");
        if (Double.compare(amount, installment.getAmountDue()) != 0)
            throw new IllegalArgumentException("Incorrect amount");
        return installmentPaymentService.processPayment(installment, amount, paymentMethod);
    }

    @Override
    public Installment updateInstallmentStatus(UUID installmentId, InstallmentStatus status) {
        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("Installment not found"));
        installment.setStatus(status);
        return installmentRepository.save(installment);
    }
}