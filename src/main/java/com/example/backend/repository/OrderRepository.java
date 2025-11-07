package com.example.backend.repository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Order;
import com.example.backend.domain.enums.OrderStatus;
import com.example.backend.domain.enums.ShipmentMethod;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(Integer userId);
    List<Order> findByOrderStatus(OrderStatus orderStatus, Sort sort);
    List<Order> findByUserIdAndOrderStatus(Integer userId, OrderStatus status);
    List<Order> findByStoreLocation_Id(Integer storeLocationId, Sort sort);
    List<Order> findByStoreLocation_IdAndOrderStatus(Integer storeLocationId, OrderStatus orderStatus, Sort sort);
    List<Order> findByOrderStatusAndKeepingExpiresAtBefore(OrderStatus orderStatus, OffsetDateTime cutoff);
    List<Order> findByShipmentMethodAndOrderStatusAndKeepingExpiresAtBefore(ShipmentMethod shipmentMethod,
                                                                            OrderStatus orderStatus,
                                                                            OffsetDateTime cutoff);
    List<Order> findByOrderStatusAndOrderDateBefore(OrderStatus orderStatus, OffsetDateTime cutoff);
}
