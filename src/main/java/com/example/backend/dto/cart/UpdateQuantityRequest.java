package com.example.backend.dto.cart;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateQuantityRequest {
    private int quantity; 
    private Boolean selected;
}
