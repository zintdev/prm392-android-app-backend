package com.example.backend.dto.vnpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "VNPayPaymentResponse", description = "VNPay payment response body")
public class VNPayPaymentResponse {
    private String paymentUrl;
    private String vnpTxnRef;
    private String message;
}
