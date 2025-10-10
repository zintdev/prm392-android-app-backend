package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "artists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Artist {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="artist_id")
    private Integer id;

    @Column(name="artist_type", nullable=false, length=50)
    private String artistType;

    @Column(name="artist_name", nullable=false, length=255)
    private String artistName;

    @Column(name="debut_year")
    private Integer debutYear;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ArtistImage> images = new java.util.ArrayList<>();

}
