package com.example.backend.service;

import com.example.backend.domain.entity.BlogPost;
import com.example.backend.dto.blog.BlogList;
import com.example.backend.dto.blog.BlogSummary;
import com.example.backend.repository.BlogPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogPostRepository blogPostRepository;

    // ✅ Constructor injection trực tiếp Repository (KHÔNG còn wrapper)
    public BlogService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    public BlogList getBlogs(Pageable pageable) {
        Page<BlogPost> page = blogPostRepository.findAll(pageable);
        return new BlogList(
                page.getContent().stream().map(this::toSummary).collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }

    private BlogSummary toSummary(BlogPost b) {
        return new BlogSummary(
                b.getId(),
                b.getTitle(),
                b.getSlug(),
                b.getStatus(),
                b.getImageUrl(),
                b.getPublishedAt(),
                (b.getAuthor() != null ? b.getAuthor().getId() : null),
                (b.getAuthor() != null ? b.getAuthor().getUsername() : null)
        );
    }
}
