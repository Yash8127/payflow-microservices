
package com.payflow.transactionservice.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.transactionservice.event.TransactionCompletedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void saveTransactionCompletedEvent(TransactionCompletedEvent event) {

		try {
			OutboxEvent outboxEvent = new OutboxEvent();

			outboxEvent.setEventId(event.referenceNumber());
			outboxEvent.setAggregateId(event.referenceNumber());
			outboxEvent.setEventType("TRANSACTION_COMPLETED");
			outboxEvent.setPayload(objectMapper.writeValueAsString(event));
			outboxEvent.setStatus("PENDING");
			outboxEvent.setAttempts(0);

			outboxEventRepository.save(outboxEvent);

		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize transaction event", ex);
		}
	}
}
