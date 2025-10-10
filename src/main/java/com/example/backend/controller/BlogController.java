package com.example.backend.controller;

import com.example.backend.dto.BlogList;
import com.example.backend.service.BlogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {
    private final BlogService blogService;

    public BlogController(BlogService blogService) { 
        this.blogService = blogService;
    }

    @GetMapping
    public BlogList list(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        return blogService.getBlogs(org.springframework.data.domain.PageRequest.of(page, size));
    }
}
