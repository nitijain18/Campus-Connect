package com.campusconnect.repository;

import com.campusconnect.entity.HomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeCategoryRepository extends JpaRepository<HomeCategory, Long> {
    List<HomeCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
}   