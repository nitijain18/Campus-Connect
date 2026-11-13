package com.campusconnect.repository;

import com.campusconnect.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByVendor_Id(Long vendorId);
    List<Event> findByCollege_Id(Long collegeId);
    List<Event> findByCategory_Id(Long categoryId);
}