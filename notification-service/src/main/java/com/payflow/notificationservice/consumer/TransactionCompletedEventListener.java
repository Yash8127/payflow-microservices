
package com.payflow.notificationservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.payflow.notificationservice.event.TransactionCompletedEvent;
import com.payflow.notificationservice.service.NotificationProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCompletedEventListener {

	private final NotificationProcessingService processingService;

	@KafkaListener(topics = "transaction-completed", groupId = "payflow-notification-group")
	public void consume(TransactionCompletedEvent event) {

		log.info("Received transaction event: {}", event.referenceNumber());

		processingService.process(event);
	}
}
