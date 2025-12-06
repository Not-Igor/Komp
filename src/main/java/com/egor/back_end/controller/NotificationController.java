package com.egor.back_end.controller;

import com.egor.back_end.dto.notification.NotificationDto;
import com.egor.back_end.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication authentication) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(authentication.getName());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(Authentication authentication) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(authentication.getName());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        Integer count = notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            notificationService.markAsRead(id, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok().build();
    }
}
