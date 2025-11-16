// package com.example.backend.controller;

// import com.example.backend.dto.payment.CreatePaymentRequest;
// import com.example.backend.dto.payment.UpdatePaymentRequest;
// import com.example.backend.dto.payment.PaymentResponse;
// import com.example.backend.domain.enums.PaymentStatus;
// import com.example.backend.service.PaymentService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/payments")
// @RequiredArgsConstructor
// @Tag(name = "Payments", description = "APIs for managing payments")
// public class PaymentController {

//     private final PaymentService paymentService;

//     // CREATE
//     @PostMapping
//     @Operation(summary = "Create payment")
//     @ApiResponses({
//             @ApiResponse(responseCode = "201", description = "Created"),
//             @ApiResponse(responseCode = "409", description = "Payment already exists for order")
//     })
//     public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
//         PaymentResponse response = paymentService.create(request);
//         return ResponseEntity.status(HttpStatus.CREATED).body(response);
//     }

//     // READ ALL
//     @GetMapping
//     @Operation(summary = "Get all payments")
//     public ResponseEntity<List<PaymentResponse>> getAll() {
//         return ResponseEntity.ok(paymentService.getAll());
//     }

//     // READ BY STATUS
//     @GetMapping("/status/{status}")
//     @Operation(summary = "Get payments by status")
//     public ResponseEntity<List<PaymentResponse>> getByStatus(@PathVariable PaymentStatus status) {
//         return ResponseEntity.ok(paymentService.getByStatus(status));
//     }

//     // READ BY USER ID
//     @GetMapping("/user/{userId}")
//     @Operation(summary = "Get payments by user ID")
//     public ResponseEntity<List<PaymentResponse>> getByUserId(@PathVariable Integer userId) {
//         return ResponseEntity.ok(paymentService.getByUserId(userId));
//     }

//     // READ ONE
//     @GetMapping("/{id}")
//     @Operation(summary = "Get payment by ID")
//     public ResponseEntity<PaymentResponse> getById(@PathVariable Integer id) {
//         return ResponseEntity.ok(paymentService.getById(id));
//     }

//     // READ BY ORDER ID
//     @GetMapping("/order/{orderId}")
//     @Operation(summary = "Get payment by order ID")
//     public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable Integer orderId) {
//         return ResponseEntity.ok(paymentService.getByOrderId(orderId));
//     }

//     // UPDATE
//     @PutMapping("/{id}")
//     @Operation(summary = "Update payment")
//     public ResponseEntity<PaymentResponse> update(
//             @PathVariable Integer id,
//             @Valid @RequestBody UpdatePaymentRequest request
//     ) {
//         return ResponseEntity.ok(paymentService.update(id, request));
//     }
// }