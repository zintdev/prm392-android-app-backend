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
import java.util.Map;

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

    // READ ALL or FILTERED
    @GetMapping
    @Operation(summary = "List products (optionally filtered) priceSort: asc|desc|low|low_to_high|up|desc|high|high_to_low|down")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    public ResponseEntity<?> list(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "publisherId", required = false) Integer publisherId,
            @RequestParam(value = "artistId", required = false) Integer artistId,
            @RequestParam(value = "priceSort", required = false) String priceSort, // asc|desc
            @RequestParam(value = "releaseYearFrom", required = false) Integer releaseYearFrom,
            @RequestParam(value = "releaseYearTo", required = false) Integer releaseYearTo,
            @RequestParam(value = "priceMin", required = false) java.math.BigDecimal priceMin,
            @RequestParam(value = "priceMax", required = false) java.math.BigDecimal priceMax
    ) {
        if (categoryId == null && publisherId == null && artistId == null && priceSort == null && releaseYearFrom == null && releaseYearTo == null && priceMin == null && priceMax == null) {
            return ResponseEntity.ok(productService.getAll());
        }
        List<Response> results = productService.filter(categoryId, publisherId, artistId, priceSort, releaseYearFrom, releaseYearTo, priceMin, priceMax);
        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", 404,
                            "message", "No products matched filters"
                    ));
        }
        return ResponseEntity.ok(results);
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
