package com.example.backend.service;

import com.example.backend.domain.entity.*;
import com.example.backend.domain.enums.CartStatus;
import com.example.backend.domain.enums.OrderStatus;
import com.example.backend.dto.order.CreateOrderRequest;
import com.example.backend.dto.order.OrderResponse;
import com.example.backend.dto.order.OrderItemResponse;
import com.example.backend.dto.order.UpdateOrderStatusRequest;
import com.example.backend.exception.custom.CartNotFoundException;
import com.example.backend.exception.custom.OrderNotFoundException;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // CREATE ORDER FROM CART
    // CREATE ORDER FROM CART
    @Transactional
    public OrderResponse createOrderFromCart(CreateOrderRequest request) {
        // Lấy giỏ hàng đang hoạt động của user
        Cart cart = cartRepository.findByUserIdAndStatus(request.getUserId(), CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for user " + request.getUserId()));

        // Xác minh user tồn tại
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo order mới
        Order order = Order.builder()
                .user(user)
                .cart(cart)
                .orderStatus(OrderStatus.PENDING)
                .shipmentMethod(request.getShipmentMethod())
                .orderDate(OffsetDateTime.now())
                .shippingFullName(request.getShippingFullName())
                .shippingPhone(request.getShippingPhone())
                .shippingAddressLine1(request.getShippingAddressLine1())
                .shippingAddressLine2(request.getShippingAddressLine2())
                .shippingCityState(request.getShippingCityState())
                .subtotal(cart.getSubtotal())
                .taxTotal(cart.getTaxTotal())
                .shippingFee(cart.getShippingFee())
                .grandTotal(cart.getGrandTotal())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Lấy danh sách sản phẩm trong cart
        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());

        for (CartItem cartItem : cartItems) {
            // Chỉ xử lý những item được chọn
            if (cartItem.isSelected()) {
                Product product = cartItem.getProduct();

                // --- Kiểm tra tồn kho ---
                if (product.getQuantity() < cartItem.getQuantity()) {
                    throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ hàng tồn kho!");
                }

                // --- Trừ tồn kho ---
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                productRepository.save(product);

                // --- Tạo order item ---
                OrderItem orderItem = OrderItem.builder()
                        .order(savedOrder)
                        .product(product)
                        .productName(product.getName())
                        .unitPrice(cartItem.getUnitPrice())
                        .quantity(cartItem.getQuantity())
                        .taxRate(cartItem.getTaxRate())
                        .currencyCode(cartItem.getCurrencyCode())
                        .build();

                orderItemRepository.save(orderItem);
            }
        }

        // Xóa những item đã chọn khỏi giỏ hàng
        List<CartItem> selectedItems = cartItems.stream()
                .filter(CartItem::isSelected)
                .toList();

        for (CartItem selectedItem : selectedItems) {
            cartItemRepository.delete(selectedItem);
        }

        return mapToOrderResponse(savedOrder);
    }

    // UPDATE ORDER STATUS
    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus newStatus = request.getOrderStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("Order status must not be null");
        }

        OrderStatus oldStatus = order.getOrderStatus();

        // Nếu status không thay đổi thì không làm gì
        if (oldStatus == newStatus) {
            return mapToOrderResponse(order);
        }

        // 🔄 Nếu chuyển sang CANCELLED thì hoàn lại stock
        if (newStatus == OrderStatus.CANCELLED) {
            List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);

            for (OrderItem item : items) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity()); // hoàn lại số lượng
                productRepository.save(product);
            }
        }

        // Cập nhật trạng thái đơn
        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponse(updatedOrder);
    }


    // GET ORDER BY orderID
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapToOrderResponse(order);
    }

    // GET ALL ORDERS
    public List<OrderResponse> getAllOrders(OrderStatus orderStatus) {
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate")
                .and(Sort.by(Sort.Direction.ASC, "orderStatus"));
        
        if (orderStatus != null) {
            return orderRepository.findByOrderStatus(orderStatus, sort).stream()
                    .map(this::mapToOrderResponse)
                    .toList();
        }
        
        return orderRepository.findAll(sort).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    // GET ORDERS BY USER ID
    public List<OrderResponse> getOrdersByUserId(Integer userId, @Nullable OrderStatus status) {
        List<Order> orders;

        if (status != null) {
            orders = orderRepository.findByUserIdAndOrderStatus(userId, status);
        } else {
            orders = orderRepository.findByUserId(userId);
        }

        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }


    // MAPPER
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .cartId(order.getCart() != null ? order.getCart().getId() : null)
                .orderStatus(order.getOrderStatus())
                .shipmentMethod(order.getShipmentMethod())
                .orderDate(order.getOrderDate())
                .shippingFullName(order.getShippingFullName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .shippingCityState(order.getShippingCityState())
                .subtotal(order.getSubtotal())
                .taxTotal(order.getTaxTotal())
                .shippingFee(order.getShippingFee())
                .grandTotal(order.getGrandTotal())
                .items(itemResponses)
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .taxRate(item.getTaxRate())
                .currencyCode(item.getCurrencyCode())
                .imageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : null)
                .build();
    }
}