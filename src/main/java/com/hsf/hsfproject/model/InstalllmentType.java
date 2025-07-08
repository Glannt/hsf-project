package com.hsf.hsfproject.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "installment_types")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstalllmentType extends BaseEntity{
    private String name;
    private int months;

    @Column(name = "interest_rate")
    private Double interestRate;

    @OneToMany(mappedBy = "installmentType")
//    @JoinColumn(name = "installment_type_id")
    private List<InstallmentPlan> installmentPlans; // List of installment plans associated with this type
}
