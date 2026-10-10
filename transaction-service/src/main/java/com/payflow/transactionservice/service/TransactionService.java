package com.payflow.transactionservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.transactionservice.client.AccountServiceClient;
import com.payflow.transactionservice.dto.AccountResponse;
import com.payflow.transactionservice.dto.AccountStatus;
import com.payflow.transactionservice.dto.InternalAmountRequest;
import com.payflow.transactionservice.dto.TransactionResponse;
import com.payflow.transactionservice.dto.TransferRequest;
import com.payflow.transactionservice.entity.Transaction;
import com.payflow.transactionservice.entity.TransactionStatus;
import com.payflow.transactionservice.entity.TransactionType;
import com.payflow.transactionservice.event.TransactionCompletedEvent;
import com.payflow.transactionservice.exception.InsufficientBalanceException;
import com.payflow.transactionservice.exception.InvalidTransactionException;
import com.payflow.transactionservice.exception.TransactionException;
import com.payflow.transactionservice.outbox.OutboxEventService;
import com.payflow.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountServiceClient accountServiceClient;
	private final OutboxEventService outboxEventService;

	public Long getAuthenticatedUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		return (Long) authentication.getPrincipal();
	}

	public void validateTransferAccounts(TransferRequest request) {

		Long authenticatedUserId = getAuthenticatedUserId();

		// Get source account through internal Account Service API
		AccountResponse sourceAccount = accountServiceClient
				.getAccountByNumberInternal(request.getSourceAccountNumber());

		// Get destination account through internal Account Service API
		AccountResponse destinationAccount = accountServiceClient
				.getAccountByNumberInternal(request.getDestinationAccountNumber());

		// Source account must belong to the authenticated user
		if (!sourceAccount.getUserId().equals(authenticatedUserId)) {
			throw new AccessDeniedException("You do not have access to the source account");
		}

		// Both accounts must be active
		if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
			throw new InvalidTransactionException("Source account is not active");
		}

		if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
			throw new InvalidTransactionException("Destination account is not active");
		}

		// Source and destination cannot be the same account
		if (sourceAccount.getId().equals(destinationAccount.getId())) {
			throw new InvalidTransactionException("Source and destination accounts must be different");
		}
	}

	@Transactional
	public TransactionResponse transfer(TransferRequest request) {

		Long authenticatedUserId = getAuthenticatedUserId();

		// 1. Get source account
		AccountResponse sourceAccount = accountServiceClient
				.getAccountByNumberInternal(request.getSourceAccountNumber());

		// 2. Get destination account
		AccountResponse destinationAccount = accountServiceClient
				.getAccountByNumberInternal(request.getDestinationAccountNumber());

		// 3. Verify source account ownership
		if (!sourceAccount.getUserId().equals(authenticatedUserId)) {
			throw new AccessDeniedException("You do not have access to the source account");
		}

		// 4. Check source account status
		if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
			throw new InvalidTransactionException("Source account is not active");
		}

		// 5. Check destination account status
		if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
			throw new InvalidTransactionException("Destination account is not active");
		}

		// 6. Prevent same-account transfer
		if (sourceAccount.getId().equals(destinationAccount.getId())) {
			throw new InvalidTransactionException("Source and destination accounts must be different");
		}

		// 7. Check sufficient balance
		if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {

			throw new InsufficientBalanceException("Insufficient balance");
		}

		// 8. Create PENDING transaction
		Transaction transaction = new Transaction();

		transaction.setReferenceNumber(generateReferenceNumber());
		transaction.setUserId(authenticatedUserId);
		transaction.setSourceAccountId(sourceAccount.getId());
		transaction.setDestinationAccountId(destinationAccount.getId());
		transaction.setAmount(request.getAmount());
		transaction.setTransactionType(TransactionType.TRANSFER);
		transaction.setStatus(TransactionStatus.PENDING);
		transaction.setDescription(request.getDescription());

		Transaction savedTransaction = transactionRepository.save(transaction);

		// 9. Prepare internal amount request
		InternalAmountRequest amountRequest = new InternalAmountRequest();

		amountRequest.setAmount(request.getAmount());

		// Tracks whether money was actually debited
		boolean debitSuccessful = false;

		try {

			// 10. Debit source account
			accountServiceClient.debitInternal(request.getSourceAccountNumber(), amountRequest);

			debitSuccessful = true;

			// 11. Credit destination account
			accountServiceClient.creditInternal(request.getDestinationAccountNumber(), amountRequest);

		} catch (Exception ex) {

			/*
			 * If debit succeeded but credit failed, restore the money to the source
			 * account.
			 */
			if (debitSuccessful) {

				try {

					accountServiceClient.creditInternal(request.getSourceAccountNumber(), amountRequest);

				} catch (Exception compensationException) {

					System.err.println("CRITICAL: Transaction compensation failed. " + "Reference: "
							+ savedTransaction.getReferenceNumber());
				}
			}

			// Mark transaction as failed
			savedTransaction.setStatus(TransactionStatus.FAILED);

			transactionRepository.save(savedTransaction);

			throw new TransactionException("Transfer failed");
		}

		// 12. Transfer completed
		savedTransaction.setStatus(TransactionStatus.COMPLETED);

		Transaction completedTransaction = transactionRepository.save(savedTransaction);

		TransactionCompletedEvent event = new TransactionCompletedEvent(completedTransaction.getReferenceNumber(),
				completedTransaction.getUserId(), completedTransaction.getSourceAccountId(),
				completedTransaction.getDestinationAccountId(), completedTransaction.getAmount(),
				completedTransaction.getTransactionType().name(), LocalDateTime.now());

		outboxEventService.saveTransactionCompletedEvent(event);

		return mapToResponse(completedTransaction);
	}

	public List<TransactionResponse> getMyTransactions() {

		Long authenticatedUserId = getAuthenticatedUserId();

		return transactionRepository.findByUserIdOrderByCreatedAtDesc(authenticatedUserId).stream()
				.map(this::mapToResponse).toList();
	}

	public TransactionResponse getTransactionByReference(String referenceNumber) {

		Long authenticatedUserId = getAuthenticatedUserId();

		Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
				.orElseThrow(() -> new TransactionException("Transaction not found"));

		if (!transaction.getUserId().equals(authenticatedUserId)) {
			throw new AccessDeniedException("You do not have access to this transaction");
		}

		return mapToResponse(transaction);
	}

	public List<TransactionResponse> getTransactionsByAccount(String accountNumber) {

		Long authenticatedUserId = getAuthenticatedUserId();

		AccountResponse account = accountServiceClient.getAccountByNumberInternal(accountNumber);

		if (!account.getUserId().equals(authenticatedUserId)) {
			throw new AccessDeniedException("You do not have access to this account");
		}

		return transactionRepository
				.findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(account.getId(), account.getId())
				.stream().map(this::mapToResponse).toList();
	}

	private String generateReferenceNumber() {

		return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
	}

	private TransactionResponse mapToResponse(Transaction transaction) {

		TransactionResponse response = new TransactionResponse();

		response.setId(transaction.getId());
		response.setReferenceNumber(transaction.getReferenceNumber());
		response.setUserId(transaction.getUserId());
		response.setSourceAccountId(transaction.getSourceAccountId());
		response.setDestinationAccountId(transaction.getDestinationAccountId());
		response.setAmount(transaction.getAmount());
		response.setTransactionType(transaction.getTransactionType());
		response.setStatus(transaction.getStatus());
		response.setDescription(transaction.getDescription());
		response.setCreatedAt(transaction.getCreatedAt());

		return response;
	}
}