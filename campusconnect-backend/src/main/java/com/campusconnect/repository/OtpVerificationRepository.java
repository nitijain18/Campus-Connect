package com.campusconnect.repository;

import com.campusconnect.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    List<OtpVerification> findByEmailOrderByCreatedAtDesc(String email);
}