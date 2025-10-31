package com.example.backend.dto.payment;

import com.example.backend.domain.enums.PaymentMethod;
import com.example.backend.domain.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "PaymentResponse", description = "Payment response body")
public class PaymentResponse {
    private Integer id;
    private Integer orderId;
    private Integer userId;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private OffsetDateTime paidAt;
}
