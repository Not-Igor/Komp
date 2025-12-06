package com.egor.back_end.repository;

import com.egor.back_end.model.Notification;
import com.egor.back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    Integer countByUserAndIsReadFalse(User user);
}
