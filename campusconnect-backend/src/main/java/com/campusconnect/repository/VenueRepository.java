package com.campusconnect.repository;

import com.campusconnect.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByCollege_Id(Long collegeId);
}