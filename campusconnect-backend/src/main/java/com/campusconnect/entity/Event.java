package com.campusconnect.entity;

import com.campusconnect.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private LocalDate eventDate;

    private LocalTime eventTime;

    private LocalDate registrationDeadline;

    private Double price;

    private Integer totalSeats;

    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.UPCOMING;

    private Boolean isOnline = false;

    private String meetLink;

    // Plain-text venue name, filled in directly by the vendor - simpler than the Venue relation below
    private String venueName;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vendor vendor;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Venue venue;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private College college;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private EventCategory category;

    private Double averageRating = 0.0;

    private Integer totalReviews = 0;

    private Double platformCommission;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.availableSeats == null) {
            this.availableSeats = this.totalSeats;
        }
    }
}