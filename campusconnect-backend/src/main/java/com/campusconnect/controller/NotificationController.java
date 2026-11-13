package com.campusconnect.controller;

import com.campusconnect.entity.Notification;
import com.campusconnect.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long userId) {
        long count = notificationRepository.countByUser_IdAndIsReadFalse(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .<ResponseEntity<?>>map(notif -> {
                    notif.setIsRead(true);
                    return ResponseEntity.ok(notificationRepository.save(notif));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found"));
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        List<Notification> unread = notificationRepository.findByUser_IdAndIsReadFalse(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        if (!notificationRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
        notificationRepository.deleteById(id);
        return ResponseEntity.ok("Notification deleted");
    }

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<?> clearAllNotifications(@PathVariable Long userId) {
        List<Notification> all = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(all);
        return ResponseEntity.ok("All notifications cleared");
    }
}