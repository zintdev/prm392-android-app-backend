package com.example.backend.dto.blog;

import com.example.backend.domain.enums.BlogStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public class BlogSummary {
    public Integer id;
    public String  title;
    public String  slug;
    public BlogStatus  status;  
    public String  imageUrl;
    public java.time.OffsetDateTime publishedAt;
    public Integer authorId;        
    public String  authorUsername;

    public BlogSummary(Integer id, String title, String slug, BlogStatus status,
                       String imageUrl, java.time.OffsetDateTime publishedAt,
                       Integer authorId, String authorUsername) {
        this.id = id; this.title = title; this.slug = slug; this.status = status;
        this.imageUrl = imageUrl; this.publishedAt = publishedAt;
        this.authorId = authorId; this.authorUsername = authorUsername;
    }
}