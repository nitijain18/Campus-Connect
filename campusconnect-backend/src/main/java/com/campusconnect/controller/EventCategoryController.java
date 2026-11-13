package com.campusconnect.controller;

import com.campusconnect.entity.EventCategory;
import com.campusconnect.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-categories")
@RequiredArgsConstructor
public class EventCategoryController {

    private final EventCategoryRepository eventCategoryRepository;

    @PostMapping
    public ResponseEntity<EventCategory> createCategory(@RequestBody EventCategory category) {
        return ResponseEntity.ok(eventCategoryRepository.save(category));
    }

    @GetMapping
    public ResponseEntity<List<EventCategory>> getAllCategories() {
        return ResponseEntity.ok(eventCategoryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        return eventCategoryRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody EventCategory updated) {
        return eventCategoryRepository.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    existing.setImageUrl(updated.getImageUrl());
                    return ResponseEntity.ok(eventCategoryRepository.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!eventCategoryRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }
        eventCategoryRepository.deleteById(id);
        return ResponseEntity.ok("Category deleted successfully");
    }
}