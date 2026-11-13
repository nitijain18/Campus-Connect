package com.campusconnect.controller;

import com.campusconnect.entity.*;
import com.campusconnect.enums.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final RazorpayClient razorpayClient;

    @PostMapping("/place/{studentId}")
    public ResponseEntity<?> placeOrder(@PathVariable Long studentId, @RequestBody Map<String, Object> body) {
        Long eventId = Long.valueOf(body.get("eventId").toString());
        Integer quantity = body.get("quantity") != null ? Integer.valueOf(body.get("quantity").toString()) : 1;

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getAvailableSeats() < quantity) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not enough seats available");
        }

        double unitPrice = event.getPrice() != null ? event.getPrice() : 0.0;
        double totalAmount = unitPrice * quantity;
        double commission = totalAmount > 0 ? totalAmount * 0.10 : 0.0;

        Order order = new Order();
        order.setStudent(student);
        order.setEvent(event);
        order.setTotalAmount(totalAmount);
        order.setPlatformCommission(commission);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setSubtotal(totalAmount);
        orderItemRepository.save(item);

        if (totalAmount <= 0) {
            return confirmFreeOrder(order, event, student, quantity);
        }

        try {
            JSONObject options = new JSONObject();
            options.put("amount", Math.round(totalAmount * 100));
            options.put("currency", "INR");
            options.put("receipt", "order_rcpt_" + order.getId());
            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(totalAmount);
            payment.setPlatformFee(commission);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod(PaymentMethod.RAZORPAY);
            payment.setRazorpayOrderId(razorpayOrder.get("id").toString());
            paymentRepository.save(payment);

            return ResponseEntity.ok(Map.of(
                    "orderId", order.getId(),
                    "razorpayOrderId", razorpayOrder.get("id").toString(),
                    "amount", totalAmount,
                    "currency", "INR"
            ));
        } catch (RazorpayException e) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Razorpay order creation failed: " + e.getMessage());
        }
    }

    private ResponseEntity<?> confirmFreeOrder(Order order, Event event, User student, int quantity) {
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(0.0);
        payment.setPlatformFee(0.0);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod(PaymentMethod.FREE);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        event.setAvailableSeats(event.getAvailableSeats() - quantity);
        eventRepository.save(event);

        Notification studentNotif = new Notification();
        studentNotif.setMessage("Booking confirmed for " + event.getTitle() + "!");
        studentNotif.setType(NotificationType.ORDER_CONFIRMED);
        studentNotif.setUser(student);
        notificationRepository.save(studentNotif);

        Notification vendorNotif = new Notification();
        vendorNotif.setMessage("New booking for your event: " + event.getTitle());
        vendorNotif.setType(NotificationType.GENERAL);
        vendorNotif.setUser(event.getVendor().getUser());
        notificationRepository.save(vendorNotif);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Order>> getOrdersByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(orderRepository.findByStudent_Id(studentId));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Order>> getOrdersByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(orderRepository.findByEvent_Id(eventId));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Order>> getOrdersByVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(orderRepository.findByEvent_Vendor_Id(vendorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            return ResponseEntity.badRequest().body("Order already cancelled/refunded");
        }

        Event event = order.getEvent();
        long hoursUntilEvent = ChronoUnit.HOURS.between(
                LocalDateTime.now(),
                event.getEventDate().atTime(event.getEventTime() != null ? event.getEventTime() : LocalTime.MIDNIGHT)
        );

        double refundPercent;
        if (hoursUntilEvent <= 2) refundPercent = 1.00;
        else if (hoursUntilEvent <= 24) refundPercent = 0.90;
        else if (hoursUntilEvent <= 48) refundPercent = 0.75;
        else if (hoursUntilEvent <= 72) refundPercent = 0.50;
        else refundPercent = 0.25;

        double refundAmount = order.getTotalAmount() * refundPercent;

        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        event.setAvailableSeats(event.getAvailableSeats() + 1);
        eventRepository.save(event);

        paymentRepository.findByOrder_Id(order.getId()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        });

        Notification notif = new Notification();
        notif.setMessage("Your booking for '" + event.getTitle() + "' was cancelled. Refund: " + (refundPercent * 100) + "%");
        notif.setType(NotificationType.GENERAL);
        notif.setUser(order.getStudent());
        notificationRepository.save(notif);

        return ResponseEntity.ok(Map.of("message", "Order cancelled", "refundAmount", refundAmount, "refundPercent", refundPercent * 100));
    }
}