package com.example.backend.dto.cart;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateQuantityRequest {
    @Min(0) private int quantity; 
    private Boolean selected;
}
