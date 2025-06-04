package com.hsf.hsfproject.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Table(name = "images")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image extends BaseEntity {

    private String imageUrl; // URL of the image
    private String altText;  // Alternative text for the image
    private String description; // Description of the image

    @ManyToOne
    @JoinColumn(name = "computer_item_id")
    private ComputerItem computerItem;

    @ManyToOne
    @JoinColumn(name = "pc_id")
    private PC pc; // Reference to the PC if applicable

}
