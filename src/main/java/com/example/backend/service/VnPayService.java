package com.example.backend.service;

import com.example.backend.config.VnPayConfig;
import com.example.backend.domain.entity.Order;
import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.enums.OrderStatus;
import com.example.backend.domain.enums.PaymentStatus;
import com.example.backend.dto.vnpay.VNPayPaymentRequest;
import com.example.backend.dto.vnpay.VNPayPaymentResponse;
import com.example.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private final VnPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;

    public VNPayPaymentResponse createPaymentUrl(VNPayPaymentRequest request) {
        try {
            // Lấy payment từ DB
            Payment payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            Order order = payment.getOrder();

            // ✅ Kiểm tra trạng thái order và payment trước khi tạo link thanh toán
            if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID) {
                throw new RuntimeException("Order cannot be paid because it is already " + order.getOrderStatus());
            }

            if (payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.PAID) {
                throw new RuntimeException("Payment already completed or failed. Cannot create new payment URL.");
            }

            // Sinh mã giao dịch duy nhất
            String vnpTxnRef = "PAY" + System.currentTimeMillis();

            // Cập nhật trạng thái payment
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            // Tạo tham số VNPay
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(request.getAmount().multiply(new BigDecimal("100")).intValue()));
            vnpParams.put("vnp_CurrCode", vnPayConfig.getCurrencyCode());
            vnpParams.put("vnp_TxnRef", vnpTxnRef);
            vnpParams.put("vnp_OrderInfo", String.valueOf(payment.getId()));
            vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnpParams.put("vnp_Locale", vnPayConfig.getLocale());
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", "127.0.0.1");

            // Tạo timestamp
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            // Hết hạn sau 15 phút
            cld.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Sort parameters & build query + hash
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }

            // Tạo secure hash
            String vnpSecureHash = hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
            String paymentUrl = vnPayConfig.getUrl() + "?" + query + "&vnp_SecureHash=" + vnpSecureHash;

            return VNPayPaymentResponse.builder()
                    .paymentUrl(paymentUrl)
                    .vnpTxnRef(vnpTxnRef)
                    .message("Payment URL created successfully")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay payment URL: " + e.getMessage(), e);
        }
    }


    public boolean validateCallback(Map<String, String> vnpParams) {
        try {
            // Lấy SecureHash gốc từ request
            String vnpSecureHash = vnpParams.remove("vnp_SecureHash");
            vnpParams.remove("vnp_SecureHashType");

            // Sắp xếp key theo alphabet
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            // Ghép lại thành chuỗi data
            StringBuilder hashData = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append("=").append(fieldValue);
                    if (i < fieldNames.size() - 1) hashData.append("&");
                }
            }

            // Hash lại bằng secret key
            String calculatedHash = hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
            return calculatedHash.equalsIgnoreCase(vnpSecureHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public String handleVnpayReturn(Map<String, String> vnpParams) {
        boolean validSignature = validateCallback(new HashMap<>(vnpParams));
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String transactionStatus = vnpParams.get("vnp_TransactionStatus");
        String txnRef = vnpParams.get("vnp_TxnRef");

        // Tìm payment
        Integer paymentId = Integer.valueOf(vnpParams.get("vnp_OrderInfo"));
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        Order order = payment.getOrder();

        if (validSignature && "00".equals(responseCode) && "00".equals(transactionStatus)) {
            if (payment.getStatus()!= PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(OffsetDateTime.now());
            }

            if (order != null && order.getOrderStatus()!= OrderStatus.PAID) {
                order.setOrderStatus(OrderStatus.PAID);
            }

            paymentRepository.save(payment);
            return "success";

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return "failed";
        }
    }


    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating HMAC SHA512: " + e.getMessage());
        }
    }
}