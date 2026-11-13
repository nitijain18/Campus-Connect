package com.campusconnect.repository;

import com.campusconnect.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByVendor_IdOrderByCreatedAtDesc(Long vendorId);
}   