package com.example.backend.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategoryRequest", description = "Category create/update request body")
public class Request {

    @NotBlank(message = "Category name is required")
    @Size(max = 120, message = "Category name must not exceed 120 characters")
    private String name;
}
