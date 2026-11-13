package com.campusconnect.repository;

import com.campusconnect.entity.EventMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventMediaRepository extends JpaRepository<EventMedia, Long> {
    List<EventMedia> findByEvent_IdOrderByDisplayOrderAsc(Long eventId);
}