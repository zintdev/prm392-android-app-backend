package com.example.backend.service;

import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.entity.Order;
import com.example.backend.domain.enums.PaymentMethod;
import com.example.backend.domain.enums.PaymentStatus;
import com.example.backend.dto.payment.CreatePaymentRequest;
import com.example.backend.dto.payment.UpdatePaymentRequest;
import com.example.backend.dto.payment.PaymentResponse;
import com.example.backend.dto.vnpay.VNPayPaymentRequest;
import com.example.backend.dto.vnpay.VNPayPaymentResponse;
import com.example.backend.exception.custom.PaymentNotFoundException;
import com.example.backend.exception.custom.PaymentAlreadyExistsException;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VnPayService vnPayService;

    // CREATE
    public PaymentResponse create(CreatePaymentRequest req) {
        //1. Kiểm tra order có tồn tại không
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        //Kiểm tra payment hiện tại của order (nếu có)
        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderId(req.getOrderId());

        Payment payment;

        if (existingPaymentOpt.isPresent()) {
            payment = existingPaymentOpt.get();
            //Nếu payment chưa FAILED thì không cho tạo lại
            if (payment.getStatus() != PaymentStatus.FAILED) {
                throw new PaymentAlreadyExistsException(
                        "Payment already exists for order " + req.getOrderId() +
                                " with status " + payment.getStatus()
                );
            }
            //Nếu payment đang FAILED → reset lại trạng thái
            payment.setStatus(PaymentStatus.PENDING);
            payment.setMethod(req.getMethod());
            payment.setAmount(req.getAmount());
            payment.setPaidAt(null); // reset thời gian thanh toán cũ nếu có
        } else {
            //Nếu chưa có payment → tạo mới
            payment = Payment.builder()
                    .order(order)
                    .method(req.getMethod())
                    .status(PaymentStatus.PENDING)
                    .amount(req.getAmount())
                    .build();
        }

        //Lưu lại payment (update hoặc insert)
        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }



    // READ ALL
    public List<PaymentResponse> getAll() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return paymentRepository.findAll(sort).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // READ BY STATUS
    public List<PaymentResponse> getByStatus(PaymentStatus status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return paymentRepository.findByStatus(status, sort).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // READ BY USER ID
    public List<PaymentResponse> getByUserId(Integer userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return paymentRepository.findByOrderUserId(userId, sort).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // READ ONE
    public PaymentResponse getById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return mapToResponse(payment);
    }

    // READ BY ORDER ID
    public PaymentResponse getByOrderId(Integer orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order " + orderId));
        return mapToResponse(payment);
    }

    // CREATE VNPAY PAYMENT
    public VNPayPaymentResponse createVNPayPayment(VNPayPaymentRequest request) {
        // Validate payment exists
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getPaymentId()));

        // Check if payment method is VNPAY
        if (payment.getMethod() != PaymentMethod.VNPAY) {
            throw new RuntimeException("Payment method is not VNPAY");
        }

        // Create VNPay payment URL
        return vnPayService.createPaymentUrl(request);
    }

    // UPDATE
    public PaymentResponse update(Integer id, UpdatePaymentRequest req) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (req.getStatus() != null) {
            payment.setStatus(req.getStatus());
            
            // If status is PAID, set paidAt timestamp
            if (req.getStatus() == PaymentStatus.PAID) {
                payment.setPaidAt(OffsetDateTime.now());
            }
        }

        return mapToResponse(paymentRepository.save(payment));
    }

    // MAPPER
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .userId(payment.getOrder().getUser().getId())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }
}