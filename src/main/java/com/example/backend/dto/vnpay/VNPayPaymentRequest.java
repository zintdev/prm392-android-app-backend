package com.example.backend.dto.vnpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "VNPayPaymentRequest", description = "VNPay payment request body")
public class VNPayPaymentRequest {
    @NotNull
    private Integer paymentId;
    @NotNull
    @Positive
    private BigDecimal amount;

    private String orderDescription;
}
