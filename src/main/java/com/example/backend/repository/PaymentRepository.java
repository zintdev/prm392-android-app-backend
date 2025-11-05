package com.example.backend.repository;

import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    List<Payment> findByStatus(PaymentStatus status, Sort sort);
    
    Optional<Payment> findByOrderId(Integer orderId);
    
    List<Payment> findByOrderUserId(Integer userId, Sort sort);

}