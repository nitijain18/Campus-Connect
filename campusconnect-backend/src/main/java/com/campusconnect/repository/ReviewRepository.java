package com.campusconnect.repository;

import com.campusconnect.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEvent_Id(Long eventId);
}