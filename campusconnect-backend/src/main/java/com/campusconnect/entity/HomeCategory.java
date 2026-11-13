package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "home_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean isActive = true;
}