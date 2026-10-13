
package com.payflow.notificationservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.notificationservice.entity.Notification;
import com.payflow.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void save(Notification notification) {
        notificationRepository.saveAndFlush(notification);
    }
}
