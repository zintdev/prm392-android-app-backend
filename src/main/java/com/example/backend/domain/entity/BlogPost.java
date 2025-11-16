package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "blog_posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlogPost {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="blog_post_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="author_user_id")
    private User author;

    @Column(nullable=false, length=255)
    private String title;

    @Column(nullable=false, length=255, unique=true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name="status", columnDefinition="blog_status", nullable=false)
    private com.example.backend.domain.enums.BlogStatus status;

    @Column(columnDefinition="TEXT")
    private String content;

    @Column(name="image_url", columnDefinition="TEXT")
    private String imageUrl;

    @Column(name="published_at")
    private OffsetDateTime publishedAt;

    @PrePersist
    public void prePersist() {{
        if (status == null) status = com.example.backend.domain.enums.BlogStatus.DRAFT;
    }}

}
