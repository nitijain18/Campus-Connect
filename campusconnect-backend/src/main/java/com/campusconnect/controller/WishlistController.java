package com.campusconnect.controller;

import com.campusconnect.entity.Event;
import com.campusconnect.entity.User;
import com.campusconnect.entity.Wishlist;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addToWishlist(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long eventId = body.get("eventId");

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        Wishlist wishlist = wishlistRepository.findByUser_Id(userId).orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setUser(user);
            return w;
        });

        wishlist.getEvents().add(event);
        wishlistRepository.save(wishlist);

        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromWishlist(@RequestParam Long userId, @RequestParam Long eventId) {
        Wishlist wishlist = wishlistRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        wishlist.getEvents().removeIf(e -> e.getId().equals(eventId));
        wishlistRepository.save(wishlist);

        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getWishlistByUser(@PathVariable Long userId) {
        return wishlistRepository.findByUser_Id(userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("events", Collections.emptyList())));
    }
}