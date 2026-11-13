package com.campusconnect.repository;

import com.campusconnect.entity.VendorReport;
import com.campusconnect.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorReportRepository extends JpaRepository<VendorReport, Long> {
    List<VendorReport> findByStatus(ReportStatus status);
}