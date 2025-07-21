package com.hsf.hsfproject.service.installment;

import com.hsf.hsfproject.model.Installment;
import com.hsf.hsfproject.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentReminderService {
    private static final Logger log = LoggerFactory.getLogger(PaymentReminderService.class);
    private final InstallmentRepository installmentRepository;

    /**
     * Daily check for installments due in the next 3 days.
     * In a real application this would send emails or notifications.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void remindUpcomingPayments() {
        LocalDate threshold = LocalDate.now().plusDays(3);
        List<Installment> upcoming = installmentRepository
                .findAll().stream()
                .filter(i -> i.getStatus() == com.hsf.hsfproject.constants.enums.InstallmentStatus.PENDING)
                .filter(i -> i.getDueDate().isBefore(threshold) || i.getDueDate().isEqual(threshold))
                .toList();
        for (Installment ins : upcoming) {
            log.info("Reminder: installment {} for order {} is due on {}", ins.getId(),
                    ins.getInstallmentPlan().getOrder().getOrderNumber(), ins.getDueDate());
        }
    }
}
