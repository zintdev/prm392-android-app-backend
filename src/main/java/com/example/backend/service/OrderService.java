package com.example.backend.service;

import com.example.backend.domain.entity.*;
import com.example.backend.domain.enums.CartStatus;
import com.example.backend.domain.enums.OrderStatus;
import com.example.backend.domain.enums.PaymentMethod;
import com.example.backend.domain.enums.ShipmentMethod;
import com.example.backend.domain.enums.UserRole;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final OrderFulfillmentSourceRepository orderFulfillmentSourceRepository;

    // CREATE ORDER FROM CART
    // CREATE ORDER FROM CART
    @Transactional
    public OrderResponse createOrderFromCart(CreateOrderRequest request) {
        Cart cart = cartRepository.findByUserIdAndStatus(request.getUserId(), CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for user " + request.getUserId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isPickup = request.getShipmentMethod() == ShipmentMethod.PICKUP;
        PaymentMethod paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required to create an order");
        }
    OrderStatus targetInitialStatus = resolveInitialStatus(paymentMethod, request.getShipmentMethod());
        StoreLocation pickupStore = null;
        OffsetDateTime keepingExpiresAt = null;

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());
        List<CartItem> selectedItems = cartItems.stream()
                .filter(CartItem::isSelected)
                .toList();

        if (selectedItems.isEmpty()) {
            throw new IllegalArgumentException("No selected items in cart to create order");
        }

        Map<Integer, List<AllocationDetail>> allocationsByProduct;
        StoreLocation assignedStore;

        if (isPickup) {
            if (request.getStoreLocationId() == null) {
                throw new IllegalArgumentException("Store location is required for pickup orders");
            }
            pickupStore = storeRepository.findById(request.getStoreLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Store location not found: " + request.getStoreLocationId()));
            assignedStore = pickupStore;
            allocationsByProduct = allocatePickupFulfillment(selectedItems, pickupStore);
        } else {
            StoreLocation hubStore = resolveHubStore();
            assignedStore = hubStore;
            allocationsByProduct = allocateHubFulfillment(selectedItems, hubStore);
        }

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
                .storeLocation(assignedStore)
                .keepingExpiresAt(keepingExpiresAt)
                .subtotal(cart.getSubtotal())
                .taxTotal(cart.getTaxTotal())
                .shippingFee(cart.getShippingFee())
                .grandTotal(cart.getGrandTotal())
                .build();

        Order savedOrder = orderRepository.save(order);
        OffsetDateTime reservationTimestamp = OffsetDateTime.now();

        for (CartItem cartItem : selectedItems) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new IllegalArgumentException("Product in cart is no longer available");
            }

            if (isPickup) {
                // soft reservation is handled via OrderFulfillmentSource records, no hard deduction yet
            }

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

            List<AllocationDetail> allocationDetails = allocationsByProduct.get(product.getId());
            if (allocationDetails == null || allocationDetails.isEmpty()) {
                throw new IllegalStateException("Missing fulfillment allocation for product " + product.getId());
            }
            for (AllocationDetail allocationDetail : allocationDetails) {
                OrderFulfillmentSource source = OrderFulfillmentSource.builder()
                        .order(savedOrder)
                        .orderItem(orderItem)
                        .storeLocation(allocationDetail.store())
                        .product(product)
                        .softReservedQuantity(allocationDetail.quantity())
                        .softReservedAt(reservationTimestamp)
                        .build();
                orderFulfillmentSourceRepository.save(source);
                savedOrder.getFulfillmentSources().add(source);
                orderItem.getFulfillmentSources().add(source);
            }
        }

        selectedItems.forEach(cartItemRepository::delete);

        if (targetInitialStatus != OrderStatus.PENDING) {
            savedOrder = applyStatusChange(savedOrder, targetInitialStatus, null, true);
        }

        return mapToOrderResponse(savedOrder);
    }

    private Map<Integer, List<AllocationDetail>> allocatePickupFulfillment(List<CartItem> selectedItems,
                                                                            StoreLocation pickupStore) {
        Map<Integer, List<AllocationDetail>> allocations = new HashMap<>();
        Map<StockKey, Integer> availabilityCache = new HashMap<>();

        for (CartItem cartItem : selectedItems) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new IllegalArgumentException("Product in cart is no longer available");
            }

            StoreInventory inventory = storeInventoryRepository
                    .findByStoreLocationIdAndProductId(pickupStore.getId(), product.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product '" + product.getName() + "' is not available at the selected store"));

            int available = getAvailableStock(pickupStore.getId(), product.getId(), availabilityCache);
            if (available < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough quantity for '" + product.getName() + "' at the selected store (on hand "
                                + inventory.getQuantity() + ", available " + available + ")");
            }

            allocations
                    .computeIfAbsent(product.getId(), key -> new ArrayList<>())
                    .add(new AllocationDetail(pickupStore, cartItem.getQuantity()));

            updateCachedAvailability(pickupStore.getId(), product.getId(), availabilityCache,
                    available - cartItem.getQuantity());
        }

        return allocations;
    }

    private Map<Integer, List<AllocationDetail>> allocateHubFulfillment(List<CartItem> selectedItems, StoreLocation hubStore) {
        Map<Integer, List<AllocationDetail>> allocations = new HashMap<>();
        Map<StockKey, Integer> availabilityCache = new HashMap<>();

        for (CartItem cartItem : selectedItems) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new IllegalArgumentException("Product in cart is no longer available");
            }

            int remaining = cartItem.getQuantity();
            if (remaining <= 0) {
                continue;
            }
            remaining -= allocateFromStore(product, remaining, hubStore, allocations, availabilityCache);

            if (remaining > 0) {
                List<StoreInventory> candidates = storeInventoryRepository
                        .findAllByProductIdOrderByQuantityDesc(product.getId(), hubStore.getId());
                for (StoreInventory candidate : candidates) {
                    StoreLocation candidateStore = candidate.getStoreLocation();
                    remaining -= allocateFromStore(product, remaining, candidateStore, allocations, availabilityCache);
                    if (remaining <= 0) {
                        break;
                    }
                }
            }

            if (remaining > 0) {
                throw new IllegalArgumentException("Không đủ hàng tồn kho cho sản phẩm '" + product.getName() + "' trong hệ thống.");
            }
        }

        return allocations;
    }

    private int allocateFromStore(Product product,
                                  int requiredQty,
                                  StoreLocation store,
                                  Map<Integer, List<AllocationDetail>> allocations,
                                  Map<StockKey, Integer> availabilityCache) {
        if (store == null || requiredQty <= 0) {
            return 0;
        }

        int available = getAvailableStock(store.getId(), product.getId(), availabilityCache);
        if (available <= 0) {
            return 0;
        }

        int allocated = Math.min(requiredQty, available);
        if (allocated <= 0) {
            return 0;
        }

        allocations
                .computeIfAbsent(product.getId(), key -> new ArrayList<>())
                .add(new AllocationDetail(store, allocated));

        updateCachedAvailability(store.getId(), product.getId(), availabilityCache, available - allocated);
        return allocated;
    }

    private int getAvailableStock(Integer storeId, Integer productId, Map<StockKey, Integer> availabilityCache) {
        StockKey key = new StockKey(storeId, productId);
        Integer cached = availabilityCache.get(key);
        if (cached != null) {
            return cached;
        }

        int onHand = storeInventoryRepository
                .findByStoreLocationIdAndProductId(storeId, productId)
                .map(StoreInventory::getQuantity)
                .orElse(0);

        Integer pendingReservations = orderFulfillmentSourceRepository.sumPendingReservedQuantity(storeId, productId);
        int available = onHand - (pendingReservations != null ? pendingReservations : 0);
        if (available < 0) {
            available = 0;
        }

        availabilityCache.put(key, available);
        return available;
    }

    private void updateCachedAvailability(Integer storeId,
                                           Integer productId,
                                           Map<StockKey, Integer> availabilityCache,
                                           int updatedValue) {
        availabilityCache.put(new StockKey(storeId, productId), Math.max(updatedValue, 0));
    }

    private StoreLocation resolveHubStore() {
        return storeRepository.findFirstByHubTrueOrderByIdAsc()
                .orElseGet(() -> storeRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No store locations are configured to fulfill delivery orders")));
    }

    private record AllocationDetail(StoreLocation store, int quantity) { }

    private record StockKey(Integer storeId, Integer productId) { }

    // UPDATE ORDER STATUS
    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus newStatus = request.getOrderStatus();
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status must not be null");
        }

        if (order.getOrderStatus() == newStatus) {
            return mapToOrderResponse(order);
        }

        User actor = null;
        if (request.getActorUserId() != null) {
            actor = userRepository.findById(request.getActorUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Actor user not found: " + request.getActorUserId()));
        }

        Order updatedOrder = applyStatusChange(order, newStatus, actor, false);
        return mapToOrderResponse(updatedOrder);
    }

    private Order applyStatusChange(Order order, OrderStatus newStatus, User actor, boolean skipAuthorization) {
        if (!skipAuthorization) {
            enforcePickupTransitionRules(order, newStatus);
        }
        authorizeStatusChange(order, newStatus, actor, skipAuthorization);

        if (requiresHardDeduction(newStatus)) {
            commitHardDeduction(order);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restoreInventoryForOrder(order);
        }

        if (newStatus == OrderStatus.KEEPING) {
            if (order.getShipmentMethod() != ShipmentMethod.PICKUP || order.getStoreLocation() == null) {
                throw new IllegalArgumentException("Only pickup orders can be kept on hold");
            }
            order.setKeepingExpiresAt(OffsetDateTime.now().plusDays(3));
        } else {
            order.setKeepingExpiresAt(null);
        }

        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }

    private void authorizeStatusChange(Order order, OrderStatus newStatus, User actor, boolean skipAuthorization) {
        if (skipAuthorization) {
            return;
        }

        boolean isPickupOrder = order.getShipmentMethod() == ShipmentMethod.PICKUP && order.getStoreLocation() != null;

        if (newStatus == OrderStatus.COMPLETED && isPickupOrder) {
            if (actor == null) {
                throw new IllegalArgumentException("Actor user is required to complete pickup orders");
            }
            if (actor.getRole() == UserRole.ADMIN) {
                return;
            }
            if (actor.getRole() == UserRole.STAFF && actor.getStoreLocation() != null
                    && actor.getStoreLocation().getId().equals(order.getStoreLocation().getId())) {
                return;
            }
            throw new IllegalArgumentException("Only the assigned store staff or an administrator can complete this pickup order");
        }

        if (newStatus == OrderStatus.CANCELLED && isPickupOrder) {
            if (actor == null) {
                throw new IllegalArgumentException("Actor user is required to cancel pickup orders");
            }
            if (actor.getRole() == UserRole.ADMIN) {
                return;
            }
            if (actor.getRole() == UserRole.STAFF && actor.getStoreLocation() != null
                    && actor.getStoreLocation().getId().equals(order.getStoreLocation().getId())) {
                return;
            }
            throw new IllegalArgumentException("Only the assigned store staff or an administrator can cancel this pickup order");
        }

        if (actor != null && actor.getRole() == UserRole.ADMIN) {
            return;
        }

        if (actor != null && actor.getRole() == UserRole.STAFF) {
            StoreLocation staffStore = actor.getStoreLocation();
            if (staffStore == null) {
                throw new IllegalArgumentException("Staff member is not associated with any store location");
            }

            StoreLocation orderStore = order.getStoreLocation();

            if (order.getShipmentMethod() == ShipmentMethod.DELIVERY) {
                if (!staffStore.isHub()) {
                    throw new IllegalArgumentException("Only hub staff can update delivery orders");
                }
                if (orderStore == null || !staffStore.getId().equals(orderStore.getId())) {
                    throw new IllegalArgumentException("Staff member is not assigned to the responsible hub");
                }
                if (!EnumSet.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.COMPLETED, OrderStatus.CANCELLED)
                        .contains(newStatus)) {
                    throw new IllegalArgumentException("Unsupported status transition for hub staff");
                }
                return;
            }

            if (order.getShipmentMethod() == ShipmentMethod.PICKUP) {
                if (orderStore == null || !staffStore.getId().equals(orderStore.getId())) {
                    throw new IllegalArgumentException("Staff member is not assigned to this store");
                }
                if (!EnumSet.of(OrderStatus.PAID, OrderStatus.KEEPING, OrderStatus.COMPLETED, OrderStatus.CANCELLED)
                        .contains(newStatus)) {
                    throw new IllegalArgumentException("Store staff can only move pickup orders to PAID, KEEPING, COMPLETED, or CANCELLED");
                }
                return;
            }

            throw new IllegalArgumentException("Staff members cannot update this order");
        }

        // system initiated or other roles: allow unless explicitly constrained above
    }

    private void enforcePickupTransitionRules(Order order, OrderStatus newStatus) {
        if (order.getShipmentMethod() != ShipmentMethod.PICKUP) {
            return;
        }

        OrderStatus current = order.getOrderStatus();
        EnumSet<OrderStatus> allowed;

        switch (current) {
            case PENDING -> allowed = EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID -> allowed = EnumSet.of(OrderStatus.KEEPING, OrderStatus.CANCELLED);
            case KEEPING -> allowed = EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED);
            case PROCESSING -> allowed = EnumSet.of(OrderStatus.KEEPING, OrderStatus.COMPLETED, OrderStatus.CANCELLED);
            default -> allowed = EnumSet.noneOf(OrderStatus.class);
        }

        if (!allowed.contains(newStatus)) {
            throw new IllegalArgumentException("Unsupported status transition for pickup orders");
        }
    }

    private boolean requiresHardDeduction(OrderStatus newStatus) {
        return newStatus == OrderStatus.PROCESSING
                || newStatus == OrderStatus.KEEPING
                || newStatus == OrderStatus.SHIPPED
                || newStatus == OrderStatus.COMPLETED;
    }

    private void commitHardDeduction(Order order) {
        List<OrderFulfillmentSource> sources = orderFulfillmentSourceRepository.findByOrderId(order.getId());
        if (sources.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (OrderFulfillmentSource source : sources) {
            Product product = source.getProduct();
            if (product == null) {
                continue;
            }
            int pending = source.getSoftReservedQuantity() - source.getHardDeductedQuantity();
            if (pending <= 0) {
                continue;
            }

            StoreInventory inventory = storeInventoryRepository
                    .findByStoreLocationIdAndProductId(
                            source.getStoreLocation().getId(),
                            product.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Inventory record missing for store " + source.getStoreLocation().getId()
                                    + " and product " + product.getId()));

            if (inventory.getQuantity() < pending) {
                throw new IllegalStateException(
                        "Không đủ hàng tồn kho tại " + source.getStoreLocation().getStoreName()
                                + " cho sản phẩm " + product.getName());
            }

            inventory.setQuantity(inventory.getQuantity() - pending);
            storeInventoryRepository.save(inventory);
            adjustProductQuantity(product, -pending);

            source.setHardDeductedQuantity(source.getHardDeductedQuantity() + pending);
            source.setHardDeductedAt(now);
            orderFulfillmentSourceRepository.save(source);
        }
    }

    private void restoreInventoryForOrder(Order order) {
        List<OrderFulfillmentSource> sources = orderFulfillmentSourceRepository.findByOrderId(order.getId());
        if (!sources.isEmpty()) {
            for (OrderFulfillmentSource source : sources) {
                int hardQty = source.getHardDeductedQuantity();
                Product product = source.getProduct();
                if (hardQty <= 0 || product == null) {
                    continue;
                }

                StoreInventory inventory = storeInventoryRepository
                        .findByStoreLocationIdAndProductId(source.getStoreLocation().getId(), product.getId())
                        .orElseGet(() -> StoreInventory.builder()
                                .storeLocation(source.getStoreLocation())
                                .product(product)
                                .quantity(0)
                                .build());
                inventory.setQuantity(inventory.getQuantity() + hardQty);
                storeInventoryRepository.save(inventory);
                adjustProductQuantity(product, hardQty);
            }
            orderFulfillmentSourceRepository.deleteAll(sources);
            order.getFulfillmentSources().clear();
            for (OrderItem item : order.getItems()) {
                item.getFulfillmentSources().clear();
            }
            return;
        }

        List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
        boolean isPickupOrder = order.getShipmentMethod() == ShipmentMethod.PICKUP && order.getStoreLocation() != null;

        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product == null) {
                continue;
            }

            if (isPickupOrder) {
                StoreInventory inventory = storeInventoryRepository
                        .findByStoreLocationIdAndProductId(order.getStoreLocation().getId(), product.getId())
                        .orElseGet(() -> StoreInventory.builder()
                                .storeLocation(order.getStoreLocation())
                                .product(product)
                                .quantity(0)
                                .build());
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                storeInventoryRepository.save(inventory);
                adjustProductQuantity(product, item.getQuantity());
            } else {
                adjustProductQuantity(product, item.getQuantity());
            }
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void cancelExpiredPickupOrders() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime pendingCutoff = now.minusDays(1);

        List<Order> holdingExpired = orderRepository
                .findByOrderStatusAndKeepingExpiresAtBefore(OrderStatus.KEEPING, now);
        for (Order order : holdingExpired) {
            applyStatusChange(order, OrderStatus.CANCELLED, null, true);
        }

        List<Order> processingExpired = orderRepository
                .findByShipmentMethodAndOrderStatusAndKeepingExpiresAtBefore(ShipmentMethod.PICKUP,
                        OrderStatus.PROCESSING, now);
        for (Order order : processingExpired) {
            applyStatusChange(order, OrderStatus.CANCELLED, null, true);
        }

        List<Order> stalePending = orderRepository
                .findByOrderStatusAndOrderDateBefore(OrderStatus.PENDING, pendingCutoff);
        for (Order order : stalePending) {
            applyStatusChange(order, OrderStatus.CANCELLED, null, true);
        }
    }


    // GET ORDER BY orderID
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapToOrderResponse(order);
    }

    // GET ALL ORDERS
    public List<OrderResponse> getAllOrders(OrderStatus orderStatus, Integer storeLocationId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate")
                .and(Sort.by(Sort.Direction.ASC, "orderStatus"));

        List<Order> orders;

        if (storeLocationId != null && orderStatus != null) {
            orders = orderRepository.findByStoreLocation_IdAndOrderStatus(storeLocationId, orderStatus, sort);
        } else if (storeLocationId != null) {
            orders = orderRepository.findByStoreLocation_Id(storeLocationId, sort);
        } else if (orderStatus != null) {
            orders = orderRepository.findByOrderStatus(orderStatus, sort);
        } else {
            orders = orderRepository.findAll(sort);
        }

        return orders.stream()
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

    StoreLocation storeLocation = order.getStoreLocation();

    OrderStatus displayStatus = resolveDisplayStatus(order);

    return OrderResponse.builder()
        .id(order.getId())
        .userId(order.getUser().getId())
        .cartId(order.getCart() != null ? order.getCart().getId() : null)
        .orderStatus(displayStatus)
        .shipmentMethod(order.getShipmentMethod())
        .orderDate(order.getOrderDate())
        .shippingFullName(order.getShippingFullName())
        .shippingPhone(order.getShippingPhone())
        .shippingAddressLine1(order.getShippingAddressLine1())
        .shippingAddressLine2(order.getShippingAddressLine2())
        .shippingCityState(order.getShippingCityState())
        .storeLocationId(storeLocation != null ? storeLocation.getId() : null)
        .storeName(storeLocation != null ? storeLocation.getStoreName() : null)
        .storeAddress(storeLocation != null ? storeLocation.getAddress() : null)
        .storeLatitude(storeLocation != null && storeLocation.getLatitude() != null
            ? storeLocation.getLatitude().doubleValue() : null)
        .storeLongitude(storeLocation != null && storeLocation.getLongitude() != null
            ? storeLocation.getLongitude().doubleValue() : null)
        .keepingExpiresAt(order.getKeepingExpiresAt())
        .subtotal(order.getSubtotal())
        .taxTotal(order.getTaxTotal())
        .shippingFee(order.getShippingFee())
        .grandTotal(order.getGrandTotal())
        .items(itemResponses)
        .build();
    }

    private OrderStatus resolveDisplayStatus(Order order) {
        if (order == null) {
            return null;
        }
        OrderStatus status = order.getOrderStatus();
        ShipmentMethod shipmentMethod = order.getShipmentMethod();
        if (shipmentMethod == ShipmentMethod.PICKUP && status == OrderStatus.PROCESSING) {
            return OrderStatus.KEEPING;
        }
        return status;
    }

    private OrderStatus resolveInitialStatus(PaymentMethod paymentMethod, ShipmentMethod shipmentMethod) {
        if (paymentMethod == PaymentMethod.COD) {
            if (shipmentMethod == ShipmentMethod.PICKUP) {
                return OrderStatus.KEEPING;
            }
            return OrderStatus.PROCESSING;
        }
        return OrderStatus.PENDING;
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

    private void adjustProductQuantity(Product product, int delta) {
        if (product == null || delta == 0) {
            return;
        }
        Integer currentQty = product.getQuantity();
        int current = currentQty != null ? currentQty : 0;
        int updated = current + delta;
        if (updated < 0) {
            updated = 0;
        }
        if (updated == current) {
            return;
        }
        product.setQuantity(updated);
        productRepository.save(product);
    }
}