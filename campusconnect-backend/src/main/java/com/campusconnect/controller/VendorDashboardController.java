package com.campusconnect.controller;

import com.campusconnect.entity.Event;
import com.campusconnect.entity.Transaction;
import com.campusconnect.entity.Vendor;
import com.campusconnect.enums.OrderStatus;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.OrderRepository;
import com.campusconnect.repository.TransactionRepository;
import com.campusconnect.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorDashboardController {

    private final VendorRepository vendorRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/{id}/earnings")
    public ResponseEntity<?> getEarnings(@PathVariable Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        List<Transaction> transactions = transactionRepository.findByVendor_IdOrderByCreatedAtDesc(id);
        YearMonth currentMonth = YearMonth.now();
        double thisMonthEarnings = transactions.stream()
                .filter(t -> YearMonth.from(t.getCreatedAt()).equals(currentMonth))
                .mapToDouble(Transaction::getAmount)
                .sum();

        List<Event> events = eventRepository.findByVendor_Id(id);
        Map<String, Double> perEventBreakdown = events.stream()
                .collect(Collectors.toMap(
                        Event::getTitle,
                        e -> orderRepository.findByEvent_Id(e.getId()).stream()
                                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
                                .mapToDouble(o -> o.getTotalAmount() * 0.90)
                                .sum()
                ));

        return ResponseEntity.ok(Map.of(
                "totalEarned", vendor.getTotalEarnings(),
                "thisMonth", thisMonthEarnings,
                "pendingPayouts", vendor.getPendingEarnings(),
                "perEventBreakdown", perEventBreakdown
        ));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<?> getAnalytics(@PathVariable Long id) {
        List<Event> events = eventRepository.findByVendor_Id(id);

        Map<String, Object> analytics = events.stream().collect(Collectors.toMap(
                Event::getTitle,
                e -> Map.of(
                        "ticketsSold", e.getTotalSeats() - e.getAvailableSeats(),
                        "revenue", (e.getTotalSeats() - e.getAvailableSeats()) * (e.getPrice() != null ? e.getPrice() : 0.0)
                )
        ));

        return ResponseEntity.ok(analytics);
    }
}