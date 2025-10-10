package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "publishers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Publisher {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="publisher_id")
    private Integer id;

    @Column(name="publisher_name", nullable=false, unique=true, length=255)
    private String name;

    @Column(name="founded_year")
    private Integer foundedYear;

}
