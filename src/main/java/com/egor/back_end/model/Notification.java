package com.egor.back_end.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "related_id")
    private Long relatedId; // ID of friend request, match, competition, etc.

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected Notification() {}

    public Notification(User user, NotificationType type, String message, Long relatedId) {
        this.user = user;
        this.type = type;
        this.message = message;
        this.relatedId = relatedId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
