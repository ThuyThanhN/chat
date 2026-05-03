package com.example.svmarket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 500)
    private String image;

    @OneToMany(mappedBy = "category")
    private List<Listing> listings;

    @Column(name = "image_public_id")
    private String imagePublicId;

}