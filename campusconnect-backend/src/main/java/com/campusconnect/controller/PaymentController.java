package com.campusconnect.controller;

import com.campusconnect.entity.*;
import com.campusconnect.enums.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import com.campusconnect.util.QRCodeUtil;
import com.razorpay.Utils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventRepository eventRepository;
    private final VendorRepository vendorRepository;
    private final TicketRepository ticketRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Transactional
    @PostMapping("/razorpay/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) {
        String razorpayOrderId = body.get("razorpay_order_id");
        String razorpayPaymentId = body.get("razorpay_payment_id");
        String razorpaySignature = body.get("razorpay_signature");

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for razorpay order id: " + razorpayOrderId));

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Verification error: " + e.getMessage());
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        Event event = order.getEvent();
        List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
        int quantity = items.isEmpty() ? 1 : items.get(0).getQuantity();

        event.setAvailableSeats(event.getAvailableSeats() - quantity);
        eventRepository.save(event);

        Vendor vendor = event.getVendor();
        double vendorShare = payment.getAmount() * 0.90;
        vendor.setTotalEarnings(vendor.getTotalEarnings() + vendorShare);
        vendor.setPendingEarnings(vendor.getPendingEarnings() + vendorShare);
        vendorRepository.save(vendor);

        Transaction txn = new Transaction();
        txn.setAmount(vendorShare);
        txn.setType(TransactionType.CREDIT);
        txn.setDescription("Earnings from booking - " + event.getTitle());
        txn.setVendor(vendor);
        txn.setOrder(order);
        transactionRepository.save(txn);

        for (int i = 0; i < quantity; i++) {
            Ticket ticket = new Ticket();
            ticket.setTicketNumber(UUID.randomUUID().toString());
            ticket.setOrder(order);
            ticket.setAttendeeName(order.getStudent().getFirstName() + " " + order.getStudent().getLastName());
            ticket.setAttendeeEmail(order.getStudent().getEmail());
            ticket.setStatus(TicketStatus.ACTIVE);
            ticket.setQrCodeUrl(QRCodeUtil.generateQRCodeBase64(ticket.getTicketNumber()));
            ticketRepository.save(ticket);
        }

        Notification studentNotif = new Notification();
        studentNotif.setMessage("Payment successful! Your booking for '" + event.getTitle() + "' is confirmed.");
        studentNotif.setType(NotificationType.PAYMENT_SUCCESS);
        studentNotif.setUser(order.getStudent());
        notificationRepository.save(studentNotif);

        Notification vendorNotif = new Notification();
        vendorNotif.setMessage("New booking payment received for '" + event.getTitle() + "'");
        vendorNotif.setType(NotificationType.PAYMENT_SUCCESS);
        vendorNotif.setUser(vendor.getUser());
        notificationRepository.save(vendorNotif);

        return ResponseEntity.ok(Map.of("message", "Payment verified, order confirmed", "orderId", order.getId()));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAllByOrderByCreatedAtDesc());
    }
}