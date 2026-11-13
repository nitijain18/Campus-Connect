package com.campusconnect.controller;

import com.campusconnect.entity.Ticket;
import com.campusconnect.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Ticket>> getTicketsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ticketRepository.findByOrder_Id(orderId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Ticket>> getTicketsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(ticketRepository.findByOrder_Student_Id(studentId));
    }

    @GetMapping("/{ticketNumber}/verify")
    public ResponseEntity<?> verifyTicket(@PathVariable String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid ticket"));
    }
}