package com.example.backend.controller;

import com.example.backend.dto.vnpay.VNPayPaymentRequest;
import com.example.backend.dto.vnpay.VNPayPaymentResponse;
import com.example.backend.service.PaymentService;
import com.example.backend.service.VnPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
@Tag(name = "VNPay", description = "VNPay payment integration APIs")
public class VnPayController {

    private final VnPayService vnPayService;
    private final PaymentService paymentService;

    // CREATE PAYMENT URL
    @PostMapping("/create-payment")
    @Operation(summary = "Create VNPay payment URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment URL created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request")
    })
    public ResponseEntity<VNPayPaymentResponse> createPayment(@Valid @RequestBody VNPayPaymentRequest request) {
        VNPayPaymentResponse response = vnPayService.createPaymentUrl(request);
        return ResponseEntity.ok(response);
    }

    // VNPAY CALLBACK
    @GetMapping("/return")
    public ResponseEntity<?> vnPayReturn(HttpServletRequest request) {
        Map<String, String> vnpParams = new HashMap<>();
        for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            vnpParams.put(name, request.getParameter(name));
        }

        String result = vnPayService.handleVnpayReturn(vnpParams);
        String responseCode = vnpParams.get("vnp_ResponseCode");

        if ("success".equals(result)) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "responseCode", responseCode,
                    "message", "Payment successful"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "responseCode", responseCode,
                    "message", "Payment failed or invalid"
            ));
        }
    }


    // GET PAYMENT STATUS
    @GetMapping("/status/{paymentId}")
    @Operation(summary = "Get payment status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable Integer paymentId) {
        try {
            var payment = paymentService.getById(paymentId);
            return ResponseEntity.ok(Map.of(
                    "paymentId", payment.getId(),
                    "status", payment.getStatus(),
                    "amount", payment.getAmount(),
                    "paidAt", payment.getPaidAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Payment not found",
                            "message", e.getMessage()
                    ));
        }
    }
}
