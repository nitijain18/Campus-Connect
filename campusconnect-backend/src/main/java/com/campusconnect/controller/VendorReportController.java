package com.campusconnect.controller;

import com.campusconnect.entity.*;
import com.campusconnect.enums.ReportStatus;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class VendorReportController {

    private final VendorReportRepository vendorReportRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final VendorRepository vendorRepository;

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody VendorReport request) {
        Long reportedById = request.getReportedBy().getId();
        User reportedBy = userRepository.findById(reportedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reportedById));

        VendorReport report = new VendorReport();
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setReportedBy(reportedBy);
        report.setStatus(ReportStatus.OPEN);

        if (request.getEvent() != null && request.getEvent().getId() != null) {
            eventRepository.findById(request.getEvent().getId()).ifPresent(report::setEvent);
        }
        if (request.getVendor() != null && request.getVendor().getId() != null) {
            vendorRepository.findById(request.getVendor().getId()).ifPresent(report::setVendor);
        }

        vendorReportRepository.save(report);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<VendorReport>> getReportsByStatus(@PathVariable ReportStatus status) {
        return ResponseEntity.ok(vendorReportRepository.findByStatus(status));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long id) {
        VendorReport report = vendorReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        report.setStatus(ReportStatus.RESOLVED);
        return ResponseEntity.ok(vendorReportRepository.save(report));
    }
}