package com.campusconnect.repository;

import com.campusconnect.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUser_Id(Long userId);
    List<Vendor> findByIsApprovedTrue();
    List<Vendor> findByIsApprovedFalse();
}