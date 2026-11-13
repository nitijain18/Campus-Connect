package com.campusconnect.repository;

import com.campusconnect.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_Id(Long orderId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    List<Payment> findAllByOrderByCreatedAtDesc();
}