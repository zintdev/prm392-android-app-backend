package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "artist_images", uniqueConstraints = @UniqueConstraint(columnNames = {"artist_id","sort_order"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArtistImage {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="image_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="artist_id")
    private Artist artist;

    @Column(columnDefinition="TEXT", nullable=false)
    private String url;

    @Column(name="sort_order", nullable=false)
    private Integer sortOrder;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {{
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (sortOrder == null) sortOrder = 0;
    }}

}
