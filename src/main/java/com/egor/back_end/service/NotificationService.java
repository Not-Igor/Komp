package com.egor.back_end.service;

import com.egor.back_end.dto.notification.NotificationDto;
import com.egor.back_end.model.*;
import com.egor.back_end.repository.NotificationRepository;
import com.egor.back_end.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void createNotification(User user, NotificationType type, String message, Long relatedId) {
        Notification notification = new Notification(user, type, message, relatedId);
        notificationRepository.save(notification);
    }

    public List<NotificationDto> getUserNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Integer getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to mark this notification as read");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getRelatedId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
