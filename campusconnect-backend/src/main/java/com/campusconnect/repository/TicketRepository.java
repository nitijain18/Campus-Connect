package com.campusconnect.repository;

import com.campusconnect.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByOrder_Id(Long orderId);
    List<Ticket> findByOrder_Student_Id(Long studentId);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
}