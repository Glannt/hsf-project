package com.hsf.hsfproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail extends BaseEntity {

    @Column(name = "unit_price")
    private Double  unitPrice; // Name of the product ordered

    private int quantity; // Quantity of the product ordered

    @Column(name = "total_price")
    private Double subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

}

