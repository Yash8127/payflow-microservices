
package com.payflow.transactionservice.event;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

	private static final String TOPIC = "transaction-completed";

	private final KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;

	public CompletableFuture<SendResult<String, TransactionCompletedEvent>> publishTransactionCompleted(
			TransactionCompletedEvent event) {

		return kafkaTemplate.send(TOPIC, event.referenceNumber(), event);
	}
}
