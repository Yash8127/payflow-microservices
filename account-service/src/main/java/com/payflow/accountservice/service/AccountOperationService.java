
package com.payflow.accountservice.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.payflow.accountservice.entity.AccountOperation;
import com.payflow.accountservice.exception.IdempotencyConflictException;
import com.payflow.accountservice.repository.AccountOperationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountOperationService {

	private final AccountOperationRepository accountOperationRepository;
	private final AccountOperationWorker accountOperationWorker;

	public AccountOperation processOperation(String idempotencyKey, String accountNumber, BigDecimal requestedAmount,
			String operationType) {

		if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 150) {
			throw new IllegalArgumentException("Invalid idempotency key");
		}

		if (accountNumber == null || accountNumber.isBlank()) {
			throw new IllegalArgumentException("Account number is required");
		}

		if (requestedAmount == null || requestedAmount.signum() <= 0) {
			throw new IllegalArgumentException("Amount must be positive");
		}

		if (!"DEBIT".equals(operationType) && !"CREDIT".equals(operationType)
				&& !"COMPENSATION".equals(operationType)) {
			throw new IllegalArgumentException("Unsupported operation type");
		}

		BigDecimal amount = requestedAmount.setScale(2, java.math.RoundingMode.HALF_UP);

		Optional<AccountOperation> existing = accountOperationRepository.findByIdempotencyKey(idempotencyKey);

		if (existing.isPresent()) {
			return validateExisting(existing.get(), accountNumber, amount, operationType);
		}

		try {
			return accountOperationWorker.processOperation(idempotencyKey, accountNumber, amount, operationType);

		} catch (DataIntegrityViolationException ex) {
			// The worker transaction has rolled back before control reaches
			// this catch block because it is a separate Spring-managed bean.
			Optional<AccountOperation> recovered = accountOperationRecoveryServiceFind(idempotencyKey);

			if (recovered.isPresent()) {
				return validateExisting(recovered.get(), accountNumber, amount, operationType);
			}

			// Do not disguise unrelated database integrity failures.
			throw ex;
		}
	}

	private final AccountOperationRecoveryService accountOperationRecoveryService;

	private AccountOperation validateExisting(AccountOperation operation, String accountNumber, BigDecimal amount,
			String operationType) {

		if (!operation.getAccountNumber().equals(accountNumber) || !operation.getOperationType().equals(operationType)
				|| operation.getAmount().compareTo(amount) != 0) {
			throw new IdempotencyConflictException("Idempotency key was already used for a different operation");
		}

		return operation;
	}

	private Optional<AccountOperation> accountOperationRecoveryServiceFind(String idempotencyKey) {

		return Optional.ofNullable(accountOperationRecoveryService.findExistingOperation(idempotencyKey));
	}
}
