package com.campusconnect.controller;

import com.campusconnect.entity.*;
import com.campusconnect.enums.EventStatus;
import com.campusconnect.enums.NotificationType;
import com.campusconnect.enums.OrderStatus;
import com.campusconnect.enums.PaymentStatus;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final VendorRepository vendorRepository;
    private final VenueRepository venueRepository;
    private final CollegeRepository collegeRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event request) {
        Long vendorId = request.getVendor().getId();
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

        if (!Boolean.TRUE.equals(vendor.getIsApproved())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vendor is not approved to create events");
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setEventTime(request.getEventTime());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setPrice(request.getPrice());
        event.setTotalSeats(request.getTotalSeats());
        event.setAvailableSeats(request.getTotalSeats());
        event.setIsOnline(request.getIsOnline());
        event.setMeetLink(request.getMeetLink());
        event.setVenueName(request.getVenueName());
        event.setVendor(vendor);
        event.setStatus(EventStatus.UPCOMING);

        if (request.getCollege() != null && request.getCollege().getId() != null) {
            Long collegeId = request.getCollege().getId();
            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));
            event.setCollege(college);
        }

        if (request.getCategory() != null && request.getCategory().getId() != null) {
            Long categoryId = request.getCategory().getId();
            EventCategory category = eventCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            event.setCategory(category);
        }

        double price = request.getPrice() != null ? request.getPrice() : 0.0;
        event.setPlatformCommission(price > 0 ? price * 0.10 : 0.0);

        eventRepository.save(event);

        vendor.setTotalEventsCreated(vendor.getTotalEventsCreated() + 1);
        vendorRepository.save(vendor);

        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return ResponseEntity.ok(event);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long college,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String keyword
    ) {
        Specification<Event> spec = EventSpecification.withFilters(category, college, minPrice, maxPrice, date, keyword);
        return ResponseEntity.ok(eventRepository.findAll(spec));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Event>> getEventsByVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(eventRepository.findByVendor_Id(vendorId));
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<Event>> getEventsByCollege(@PathVariable Long collegeId) {
        return ResponseEntity.ok(eventRepository.findByCollege_Id(collegeId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(eventRepository.findByCategory_Id(categoryId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event updated) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setEventDate(updated.getEventDate());
        existing.setEventTime(updated.getEventTime());
        existing.setRegistrationDeadline(updated.getRegistrationDeadline());
        existing.setPrice(updated.getPrice());
        existing.setTotalSeats(updated.getTotalSeats());
        existing.setIsOnline(updated.getIsOnline());
        existing.setMeetLink(updated.getMeetLink());
        existing.setVenueName(updated.getVenueName());

        if (updated.getCategory() != null && updated.getCategory().getId() != null) {
            eventCategoryRepository.findById(updated.getCategory().getId()).ifPresent(existing::setCategory);
        }

        eventRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @PatchMapping("/{id}/remove")
    public ResponseEntity<?> removeEvent(@PathVariable Long id, @RequestBody String reason) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);

        Notification vendorNotif = new Notification();
        vendorNotif.setMessage("Your event '" + event.getTitle() + "' was removed. Reason: " + reason);
        vendorNotif.setType(NotificationType.EVENT_REMOVED);
        vendorNotif.setUser(event.getVendor().getUser());
        notificationRepository.save(vendorNotif);

        List<Order> orders = orderRepository.findByEvent_Id(id);
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
                order.setStatus(OrderStatus.REFUNDED);
                orderRepository.save(order);

                paymentRepository.findByOrder_Id(order.getId()).ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(payment);
                });

                Notification studentNotif = new Notification();
                studentNotif.setMessage("Event '" + event.getTitle() + "' was cancelled. Your booking has been refunded.");
                studentNotif.setType(NotificationType.EVENT_REMOVED);
                studentNotif.setUser(order.getStudent());
                notificationRepository.save(studentNotif);
            }
        }

        return ResponseEntity.ok("Event removed, affected bookings refunded and notified");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
        return ResponseEntity.ok("Event deleted successfully");
    }
}