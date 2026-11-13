package com.campusconnect.controller;

import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventMedia;
import com.campusconnect.repository.EventMediaRepository;
import com.campusconnect.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-media")
@RequiredArgsConstructor
public class EventMediaController {

    private final EventMediaRepository eventMediaRepository;
    private final EventRepository eventRepository;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventMedia>> getMediaByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventMediaRepository.findByEvent_IdOrderByDisplayOrderAsc(eventId));
    }

    @PutMapping("/{id}/reorder")
    public ResponseEntity<?> reorderMedia(@PathVariable Long id, @RequestBody Integer displayOrder) {
        return eventMediaRepository.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    existing.setDisplayOrder(displayOrder);
                    return ResponseEntity.ok(eventMediaRepository.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Media not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedia(@PathVariable Long id) {
        if (!eventMediaRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Media not found");
        }
        eventMediaRepository.deleteById(id);
        return ResponseEntity.ok("Media deleted successfully");
    }
}