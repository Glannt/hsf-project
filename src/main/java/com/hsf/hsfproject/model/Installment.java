package com.hsf.hsfproject.model;

import com.hsf.hsfproject.constants.enums.InstallmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment extends BaseEntity {

    private LocalDate dueDate;
    private LocalDate paidDate;
    private double amountDue;

    @Enumerated(EnumType.STRING)
    private InstallmentStatus status;

    @OneToOne
    private Transaction transaction; // Reference to the associated transaction

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_plan_id")
    private InstallmentPlan installmentPlan;
}

