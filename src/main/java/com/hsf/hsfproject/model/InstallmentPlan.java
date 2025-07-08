package com.hsf.hsfproject.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentPlan extends BaseEntity {
    private String planName;

    @Column(name = "start_date")
    private Date startDate; // Start date of the installment plan

    @Column(name = "monthly_payment")
    private double monthlyPayment; // Monthly payment amount


    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_type_id")
    private InstalllmentType installmentType;

    @OneToMany(mappedBy = "installmentPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "installment_plan_id")
    private List<Installment> installments = new ArrayList<>();
}

