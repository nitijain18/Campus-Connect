package com.campusconnect.entity;

import com.campusconnect.enums.TicketStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @Column(columnDefinition = "TEXT")
private String qrCodeUrl;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Order order;

    private String attendeeName;

    private String attendeeEmail;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.ticketNumber == null) {
            this.ticketNumber = java.util.UUID.randomUUID().toString();
        }
    }
}