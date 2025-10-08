package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "artists",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_artists_artist_name", columnNames = "artist_name"),
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    private Integer id;

    @Column(name = "artist_name", nullable = false, length = 100)
    private String name;

    @Column(name = "debut_year",nullable = false)
    private Integer debutyear;

    @Column(name = "artist_type", nullable = false, length = 50)
    private String type;
}
