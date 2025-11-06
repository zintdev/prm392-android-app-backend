package com.example.backend.controller;

import com.example.backend.dto.order.CreateOrderRequest;
import com.example.backend.dto.order.OrderResponse;
import com.example.backend.dto.order.UpdateOrderStatusRequest;
import com.example.backend.domain.enums.OrderStatus;
import com.example.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    // CREATE ORDER FROM CART
    @PostMapping
    @Operation(summary = "Create order from cart")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrderFromCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // UPDATE ORDER STATUS
    @PutMapping("/{orderId}")
    @Operation(summary = "Update order status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> updateOrderStatus(
        @PathVariable("orderId") Integer orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse order = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(order);
    }

    // GET ORDER BY ID
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderId") Integer orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // GET ALL ORDERS
    @GetMapping
    @Operation(summary = "Get all orders (optionally filtered by status)")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus
    ) {
        List<OrderResponse> orders = orderService.getAllOrders(orderStatus);
        return ResponseEntity.ok(orders);
    }

    // GET /orders/user/{userId}?status=PENDING
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID (optionally filter by status)")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
        @PathVariable("userId") Integer userId,
            @RequestParam(name = "status", required = false) OrderStatus orderStatus
    ) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId, orderStatus);
        return ResponseEntity.ok(orders);
    }
}