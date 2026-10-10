
package com.payflow.transactionservice.outbox;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

	private final OutboxEventRepository outboxEventRepository;
	private final OutboxEventProcessor outboxEventProcessor;

	@Scheduled(fixedDelay = 5000)
	public void publishPendingEvents() {

		List<OutboxEvent> events = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING");

		for (OutboxEvent event : events) {
			outboxEventProcessor.publishOne(event);
		}
	}
}
