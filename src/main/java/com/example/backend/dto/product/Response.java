package com.example.backend.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "ProductResponse", description = "Product response body")
public class Response {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private String artistName;
    private String publisherName;
    private String categoryName;
    private LocalDate releaseDate;
    private OffsetDateTime createdAt;
}
