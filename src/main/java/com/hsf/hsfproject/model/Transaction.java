package com.hsf.hsfproject.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "transactions")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {


    private LocalDateTime transactionDate; // Date of the transaction
    private double totalAmount; // Total amount of the transaction
    private String paymentMethod; // Payment method used (e.g., credit card, PayPal)
    private String status; // Status of the transaction (e.g., completed, pending, failed)
    private String transactionRef;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Additional fields can be added as needed
}

