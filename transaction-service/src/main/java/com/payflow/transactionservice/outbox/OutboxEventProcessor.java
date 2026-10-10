
package com.payflow.transactionservice.outbox;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.transactionservice.event.TransactionCompletedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventProcessor {

	private static final String TOPIC = "transaction-completed";

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Transactional
	public void publishOne(OutboxEvent outboxEvent) {

		try {
			TransactionCompletedEvent event = objectMapper.readValue(outboxEvent.getPayload(),
					TransactionCompletedEvent.class);

			kafkaTemplate.send(TOPIC, event.referenceNumber(), event).get(10, TimeUnit.SECONDS);

			outboxEvent.setStatus("PUBLISHED");
			outboxEvent.setPublishedAt(LocalDateTime.now());
			outboxEvent.setLastError(null);

		} catch (Exception ex) {
			outboxEvent.setAttempts(outboxEvent.getAttempts() + 1);
			outboxEvent.setLastError(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());

			System.err.println(
					"Outbox publishing failed for " + outboxEvent.getAggregateId() + ": " + outboxEvent.getLastError());
		}

		outboxEventRepository.save(outboxEvent);
	}
}
