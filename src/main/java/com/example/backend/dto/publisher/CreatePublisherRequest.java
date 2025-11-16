package com.example.backend.dto.publisher;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePublisherRequest {
    
    @NotBlank(message = "Publisher name cannot be blank")
    @Size(max = 100, message = "Publisher name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Founded year cannot be null")
    @Min(value = 1000, message = "Founded year must be at least 1000")
    @Max(value = 2025, message = "Founded year must not exceed 2025")
    private Integer foundedYear;
}
