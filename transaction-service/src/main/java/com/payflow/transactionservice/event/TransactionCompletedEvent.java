
package com.payflow.transactionservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCompletedEvent(
        String referenceNumber,
        Long userId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String transactionType,
        LocalDateTime completedAt) {
}
