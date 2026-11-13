package com.campusconnect.controller;

import com.campusconnect.entity.College;
import com.campusconnect.entity.Venue;
import com.campusconnect.repository.CollegeRepository;
import com.campusconnect.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueRepository venueRepository;
    private final CollegeRepository collegeRepository;

    @PostMapping
    public ResponseEntity<?> createVenue(@RequestBody Venue request) {
        if (request.getCollege() == null || request.getCollege().getId() == null) {
            return ResponseEntity.badRequest().body("College is required");
        }
        College college = collegeRepository.findById(request.getCollege().getId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCapacity(request.getCapacity());
        venue.setAmenities(request.getAmenities());
        venue.setImageUrl(request.getImageUrl());
        venue.setCollege(college);

        return ResponseEntity.ok(venueRepository.save(venue));
    }

    @GetMapping
    public ResponseEntity<List<Venue>> getAllVenues() {
        return ResponseEntity.ok(venueRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVenueById(@PathVariable Long id) {
        return venueRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found"));
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<Venue>> getVenuesByCollege(@PathVariable Long collegeId) {
        return ResponseEntity.ok(venueRepository.findByCollege_Id(collegeId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVenue(@PathVariable Long id, @RequestBody Venue updated) {
        return venueRepository.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    existing.setName(updated.getName());
                    existing.setAddress(updated.getAddress());
                    existing.setCapacity(updated.getCapacity());
                    existing.setAmenities(updated.getAmenities());
                    existing.setImageUrl(updated.getImageUrl());
                    return ResponseEntity.ok(venueRepository.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVenue(@PathVariable Long id) {
        if (!venueRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found");
        }
        venueRepository.deleteById(id);
        return ResponseEntity.ok("Venue deleted successfully");
    }
}