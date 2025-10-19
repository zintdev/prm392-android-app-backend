package com.example.backend.service;

import com.example.backend.domain.entity.Category;
import com.example.backend.dto.category.Request;
import com.example.backend.dto.category.Response;
import com.example.backend.exception.custom.CategoryAlreadyExistsException;
import com.example.backend.exception.custom.CategoryNotFoundException;
import com.example.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Response create(Request request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException(request.getName());
        }
        Category saved = categoryRepository.save(Category.builder().name(request.getName()).build());
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<Response> getAll() {
        return categoryRepository.findAll().stream().map(this::map).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Response getById(Integer id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return map(cat);
    }

    @Transactional
    public Response update(Integer id, Request request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (!existing.getName().equalsIgnoreCase(request.getName())
                    && categoryRepository.existsByNameIgnoreCase(request.getName())) {
                throw new CategoryAlreadyExistsException(request.getName());
            }

            existing.setName(request.getName().trim());
        }
        return map(categoryRepository.save(existing));
    }

    @Transactional
    public void delete(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }

    private Response map(Category category) {
        return Response.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
