package com.example.backend.dto.payment;

import com.example.backend.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "CreatePaymentRequest", description = "Create payment request body")
public class CreatePaymentRequest {
    @NotNull
    private Integer orderId;
    
    @NotNull
    private PaymentMethod method;
    
    @NotNull
    @Positive
    private BigDecimal amount;
}
