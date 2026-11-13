package com.campusconnect.controller;

import com.campusconnect.entity.HomeCategory;
import com.campusconnect.repository.HomeCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/home-categories")
@RequiredArgsConstructor
public class HomeCategoryController {

    private final HomeCategoryRepository homeCategoryRepository;

    @PostMapping
    public ResponseEntity<HomeCategory> createHomeCategory(@RequestBody HomeCategory category) {
        return ResponseEntity.ok(homeCategoryRepository.save(category));
    }

    @GetMapping
    public ResponseEntity<List<HomeCategory>> getAllHomeCategories() {
        return ResponseEntity.ok(homeCategoryRepository.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<HomeCategory>> getActiveHomeCategories() {
        return ResponseEntity.ok(homeCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHomeCategoryById(@PathVariable Long id) {
        return homeCategoryRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Home category not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHomeCategory(@PathVariable Long id, @RequestBody HomeCategory updated) {
        return homeCategoryRepository.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    existing.setName(updated.getName());
                    existing.setImageUrl(updated.getImageUrl());
                    existing.setDisplayOrder(updated.getDisplayOrder());
                    existing.setIsActive(updated.getIsActive());
                    return ResponseEntity.ok(homeCategoryRepository.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Home category not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHomeCategory(@PathVariable Long id) {
        if (!homeCategoryRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Home category not found");
        }
        homeCategoryRepository.deleteById(id);
        return ResponseEntity.ok("Home category not found");
    }
}