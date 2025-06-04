package com.hsf.hsfproject.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Table(name = "cart")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {



    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "item_count")
    private Integer itemCount;

    @OneToOne(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "cart_id")
    private Set<CartItem> cartItems;



    // Add other fields and relationships as needed

}

