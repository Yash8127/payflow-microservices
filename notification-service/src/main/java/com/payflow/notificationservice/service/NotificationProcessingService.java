package com.payflow.notificationservice.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.payflow.notificationservice.entity.Notification;
import com.payflow.notificationservice.event.TransactionCompletedEvent;
import com.payflow.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessingService {

	private final NotificationRepository notificationRepository;
	private final NotificationPersistenceService persistenceService;

	public void process(TransactionCompletedEvent event) {

		String eventId = event.referenceNumber();

		if (notificationRepository.existsByEventId(eventId)) {
			log.info("Duplicate event ignored: {}", eventId);
			return;
		}

		String message = "Transfer of " + event.amount() + " completed successfully.";

		Notification notification = new Notification(eventId, event.userId(), message);

		try {
			persistenceService.save(notification);
			log.info("Notification saved for event: {}", eventId);

		} catch (DataIntegrityViolationException ex) {

			if (notificationRepository.existsByEventId(eventId)) {
				log.info("Concurrent duplicate event ignored: {}", eventId);
				return;
			}

			throw ex;
		}
	}
}
