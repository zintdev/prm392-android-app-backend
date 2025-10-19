package com.example.backend.controller;

import com.example.backend.dto.product.Request;
import com.example.backend.dto.product.Response;
import com.example.backend.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    // CREATE
    @PostMapping
    @Operation(summary = "Create product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created")
    })
    public ResponseEntity<Response> create(@Valid @RequestBody Request request) {
        Response response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ ALL
    @GetMapping
    @Operation(summary = "List all products")
    public ResponseEntity<List<Response>> list() {
        return ResponseEntity.ok(productService.getAll());
    }

    // READ ONE
    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ResponseEntity<Response> get(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<Response> update(
            @PathVariable Integer id,
            @Valid @RequestBody Request request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.ok().build();
    }
}
