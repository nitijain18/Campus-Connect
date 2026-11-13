package com.campusconnect.repository;

import com.campusconnect.entity.Event;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class EventSpecification {

    public static Specification<Event> withFilters(Long categoryId, Long collegeId, Double minPrice,
                                                     Double maxPrice, LocalDate date, String keyword) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), categoryId));
            }
            if (collegeId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("college").get("id"), collegeId));
            }
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (date != null) {
                predicate = cb.and(predicate, cb.equal(root.get("eventDate"), date));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}