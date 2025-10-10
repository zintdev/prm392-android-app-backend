package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "publishers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_publishers_publisher_name", columnNames = "publisher_name"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private Integer id;

    @Column(name = "publisher_name", nullable = false, length = 100)
    private String name;

    @Column(name = "founded_year",nullable = false)
    private Integer foundedyear;
}
