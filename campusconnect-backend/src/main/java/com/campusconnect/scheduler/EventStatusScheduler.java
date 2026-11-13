package com.campusconnect.scheduler;

import com.campusconnect.entity.Event;
import com.campusconnect.enums.EventStatus;
import com.campusconnect.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 * * * *") // every hour
    @Transactional
    public void updateEventStatuses() {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findAll();

        for (Event event : events) {
            if (event.getStatus() == EventStatus.UPCOMING && event.getEventDate().isEqual(today)) {
                event.setStatus(EventStatus.ONGOING);
            } else if (event.getStatus() != EventStatus.CANCELLED && event.getEventDate().isBefore(today)) {
                event.setStatus(EventStatus.COMPLETED);
            }
        }
        eventRepository.saveAll(events);
    }
}