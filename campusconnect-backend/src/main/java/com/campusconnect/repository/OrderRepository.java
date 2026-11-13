package com.campusconnect.repository;

import com.campusconnect.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStudent_Id(Long studentId);
    List<Order> findByEvent_Id(Long eventId);
    List<Order> findByEvent_Vendor_Id(Long vendorId);
}