package com.example.backend.dto.vnpay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "VNPayCallbackRequest", description = "VNPay callback request body")
public class VNPayCallbackRequest {
    private String vnpTxnRef;
    private String vnpAmount;
    private String vnpOrderInfo;
    private String vnpResponseCode;
    private String vnpTransactionStatus;
    private String vnpTxnId;
    private String vnpSecureHash;
}
