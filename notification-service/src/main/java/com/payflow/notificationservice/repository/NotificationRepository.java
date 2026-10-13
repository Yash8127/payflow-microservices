
package com.payflow.notificationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payflow.notificationservice.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Optional<Notification> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
}
