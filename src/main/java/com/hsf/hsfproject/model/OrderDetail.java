package com.hsf.hsfproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Column(name = "product_name")
    private String productName; // Name of the product ordered

    @Column(name = "unit_price")
    private Double  unitPrice; // Name of the product ordered

    private int quantity; // Quantity of the product ordered

    @Column(name = "total_price")
    private Double subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    @ManyToOne
    @JoinColumn(name = "computer_item_id")
    @JsonBackReference
    private ComputerItem computerItem; // Reference to the computer item ordered

    @ManyToOne
    @JoinColumn(name = "pc_id")
    @JsonBackReference
    private PC pc; // Reference to the PC ordered

}

