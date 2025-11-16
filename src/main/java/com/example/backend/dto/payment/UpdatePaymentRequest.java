package com.example.backend.dto.payment;

import com.example.backend.domain.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "UpdatePaymentRequest", description = "Update payment request body")
public class UpdatePaymentRequest {
    private PaymentStatus status;
}
