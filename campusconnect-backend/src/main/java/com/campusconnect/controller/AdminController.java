package com.campusconnect.controller;

import com.campusconnect.enums.ReportStatus;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final VendorReportRepository vendorReportRepository;

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", userRepository.count(),
                "totalVendors", vendorRepository.count(),
                "pendingVendorApprovals", vendorRepository.findByIsApprovedFalse().size(),
                "totalEvents", eventRepository.count(),
                "totalOrders", orderRepository.count(),
                "openReports", vendorReportRepository.findByStatus(ReportStatus.OPEN).size()
        ));
    }
}