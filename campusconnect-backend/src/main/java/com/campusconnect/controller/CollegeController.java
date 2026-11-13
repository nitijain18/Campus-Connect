package com.campusconnect.controller;

import com.campusconnect.entity.College;
import com.campusconnect.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colleges")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeRepository collegeRepository;

    @PostMapping
    public ResponseEntity<College> createCollege(@RequestBody College college) {
        return ResponseEntity.ok(collegeRepository.save(college));
    }

    @GetMapping
    public ResponseEntity<List<College>> getAllColleges() {
        return ResponseEntity.ok(collegeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollegeById(@PathVariable Long id) {
        return collegeRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("College not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollege(@PathVariable Long id, @RequestBody College updated) {
        return collegeRepository.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    existing.setName(updated.getName());
                    existing.setCity(updated.getCity());
                    existing.setState(updated.getState());
                    existing.setAddress(updated.getAddress());
                    existing.setWebsite(updated.getWebsite());
                    existing.setLogoUrl(updated.getLogoUrl());
                    return ResponseEntity.ok(collegeRepository.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("College not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollege(@PathVariable Long id) {
        if (!collegeRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("College not found");
        }
        collegeRepository.deleteById(id);
        return ResponseEntity.ok("College deleted successfully");
    }
}