package com.campusconnect.controller;

import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.entity.Vendor;
import com.campusconnect.enums.NotificationType;
import com.campusconnect.enums.UserRole;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @PostMapping
    public ResponseEntity<?> applyAsVendor(@RequestBody Vendor request) {
        Long userId = request.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (vendorRepository.findByUser_Id(userId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Vendor application already exists for this user");
        }

        Vendor vendor = new Vendor();
        vendor.setBusinessName(request.getBusinessName());
        vendor.setDescription(request.getDescription());
        vendor.setCity(request.getCity());
        vendor.setGstin(request.getGstin());
        vendor.setBankAccountNumber(request.getBankAccountNumber());
        vendor.setIfscCode(request.getIfscCode());
        vendor.setAccountHolderName(request.getAccountHolderName());
        vendor.setUser(user);
        vendor.setIsApproved(false);

        vendorRepository.save(vendor);

        user.setRole(UserRole.ROLE_VENDOR);
        userRepository.save(user);

        List<User> admins = userRepository.findByIsAdminTrue();
        for (User admin : admins) {
            Notification adminNotif = new Notification();
            adminNotif.setMessage("New vendor application from " + user.getFirstName() + " " + user.getLastName());
            adminNotif.setType(NotificationType.GENERAL);
            adminNotif.setUser(admin);
            notificationRepository.save(adminNotif);
        }

        Notification vendorNotif = new Notification();
        vendorNotif.setMessage("Your vendor application has been submitted and is pending review.");
        vendorNotif.setType(NotificationType.GENERAL);
        vendorNotif.setUser(user);
        notificationRepository.save(vendorNotif);

        return ResponseEntity.ok(vendor);
    }

    @GetMapping
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(vendorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVendorById(@PathVariable Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getVendorByUserId(@PathVariable Long userId) {
        Vendor vendor = vendorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for user id: " + userId));
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Vendor>> getPendingVendors() {
        return ResponseEntity.ok(vendorRepository.findByIsApprovedFalse());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<Vendor>> getApprovedVendors() {
        return ResponseEntity.ok(vendorRepository.findByIsApprovedTrue());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveVendor(@PathVariable Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setIsApproved(true);
        vendor.setRejectionReason(null);
        vendorRepository.save(vendor);

        Notification notif = new Notification();
        notif.setMessage("Congratulations! Your vendor application has been approved.");
        notif.setType(NotificationType.VENDOR_APPROVED);
        notif.setUser(vendor.getUser());
        notificationRepository.save(notif);

        return ResponseEntity.ok(vendor);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectVendor(@PathVariable Long id, @RequestBody String reason) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setIsApproved(false);
        vendor.setRejectionReason(reason);
        vendorRepository.save(vendor);

        Notification notif = new Notification();
        notif.setMessage("Your vendor application was rejected. Reason: " + reason);
        notif.setType(NotificationType.GENERAL);
        notif.setUser(vendor.getUser());
        notificationRepository.save(notif);

        return ResponseEntity.ok(vendor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVendor(@PathVariable Long id, @RequestBody Vendor updatedVendor) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        existing.setBusinessName(updatedVendor.getBusinessName());
        existing.setDescription(updatedVendor.getDescription());
        existing.setCity(updatedVendor.getCity());
        existing.setGstin(updatedVendor.getGstin());
        existing.setBankAccountNumber(updatedVendor.getBankAccountNumber());
        existing.setIfscCode(updatedVendor.getIfscCode());
        existing.setAccountHolderName(updatedVendor.getAccountHolderName());

        vendorRepository.save(existing);
        return ResponseEntity.ok(existing);
    }
}