package com.example.backend.dto.store;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreLocationRequest {

    // ==================================
    // === THÊM TRƯỜNG NÀY VÀO ===
    @NotBlank @Size(max=150)
    private String storeName; // Tên cửa hàng (ví dụ: "CD Store Quận 1")
    // ==================================

    @NotNull @DecimalMin(value="-90.0")  @DecimalMax(value="90.0")
    private BigDecimal latitude;

    @NotNull @DecimalMin(value="-180.0") @DecimalMax(value="180.0")
    private BigDecimal longitude;

    @NotBlank @Size(max=255)
    private String address; // Địa chỉ đầy đủ (ví dụ: "Chợ Bến Thành, Quận 1...")
}
