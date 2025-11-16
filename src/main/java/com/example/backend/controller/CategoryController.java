package com.example.backend.controller;

import com.example.backend.dto.category.Request;
import com.example.backend.dto.category.Response;
import com.example.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "APIs for managing categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created")
    })
    public ResponseEntity<Response> create(@Valid @RequestBody Request request) {
        Response response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List categories")
    public ResponseEntity<List<Response>> list() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    public ResponseEntity<Response> get(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<Response> update(@PathVariable Integer id, @Valid @RequestBody Request request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.ok().build();
    }
}
