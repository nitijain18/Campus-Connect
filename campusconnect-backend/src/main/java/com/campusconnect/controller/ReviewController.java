package com.campusconnect.controller;

import com.campusconnect.entity.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Review request) {
        Long eventId = request.getEvent().getId();
        Long reviewerId = request.getReviewer().getId();
        Long orderId = request.getOrder().getId();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reviewerId));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Review review = new Review();
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setEvent(event);
        review.setReviewer(reviewer);
        review.setOrder(order);
        reviewRepository.save(review);

        List<Review> allReviews = reviewRepository.findByEvent_Id(event.getId());
        double avg = allReviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
        event.setAverageRating(avg);
        event.setTotalReviews(allReviews.size());
        eventRepository.save(event);

        return ResponseEntity.ok(review);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Review>> getReviewsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(reviewRepository.findByEvent_Id(eventId));
    }
}