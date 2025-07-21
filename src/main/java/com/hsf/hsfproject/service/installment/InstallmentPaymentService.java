package com.hsf.hsfproject.service.installment;

import com.hsf.hsfproject.constants.enums.InstallmentStatus;
import com.hsf.hsfproject.model.Installment;
import com.hsf.hsfproject.model.Transaction;
import com.hsf.hsfproject.repository.InstallmentRepository;
import com.hsf.hsfproject.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstallmentPaymentService {

    private final InstallmentRepository installmentRepository;
    private final TransactionRepository transactionRepository;

    public Installment processPayment(Installment installment, double amount, String paymentMethod) {
        Transaction transaction = Transaction.builder()
                .order(installment.getInstallmentPlan().getOrder())
                .totalAmount(amount)
                .transactionDate(LocalDateTime.now())
                .paymentMethod(paymentMethod)
                .status("PAID")
                .transactionRef(UUID.randomUUID().toString())
                .build();
        transaction.setInstallment(installment);
        transactionRepository.save(transaction);

        installment.setTransaction(transaction);
        installment.setPaidDate(LocalDate.now());
        installment.setStatus(InstallmentStatus.PAID);
        return installmentRepository.save(installment);
    }
}