package com.egor.back_end.dto.notification;

import com.egor.back_end.model.NotificationType;
import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        NotificationType type,
        String message,
        Long relatedId,
        boolean isRead,
        LocalDateTime createdAt
) {}
