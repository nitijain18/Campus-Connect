package com.campusconnect.entity;

import com.campusconnect.enums.ReportStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reason;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "reported_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User reportedBy;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Event event;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vendor vendor;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}