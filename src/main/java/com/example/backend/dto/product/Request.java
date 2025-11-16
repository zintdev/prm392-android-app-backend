package com.example.backend.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "ProductRequest", description = "Product create/update request body")
public class Request {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private Integer artistId;
    private Integer publisherId;
    private Integer categoryId;
    private LocalDate releaseDate;
}
