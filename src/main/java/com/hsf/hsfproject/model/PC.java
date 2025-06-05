package com.hsf.hsfproject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Table(name = "pc")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PC extends BaseEntity {

    private String name;

    private String description;

    @OneToMany(mappedBy = "pc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ComputerItem> computerItems = new ArrayList<>();

    @OneToMany(mappedBy = "pc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "pc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    @Column(name = "total_price")
    private Double totalPrice;


}

