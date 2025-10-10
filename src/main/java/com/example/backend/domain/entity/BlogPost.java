package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "blog_posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlogPost {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "blog_post_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_user_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_blog_author"))
  private User author;

  @NotBlank
  @Column(name = "title", length = 255, nullable = false)
  private String title;

  @NotBlank
  @Column(name = "slug", length = 255, nullable = false, unique = true)
  private String slug;


  @Column(name = "status", length = 20, nullable = false)
  private String status = "DRAFT";

  @Column(name = "content")
  private String content;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "published_at")
  private OffsetDateTime publishedAt;
}
