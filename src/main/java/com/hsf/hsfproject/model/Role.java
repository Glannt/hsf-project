package com.hsf.hsfproject.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Table(name = "roles")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity{

    private String name;

    private String description;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "role_id")
    private Set<User> users;
}

