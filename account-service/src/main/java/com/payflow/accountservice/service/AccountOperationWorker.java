
package com.payflow.accountservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.accountservice.entity.Account;
import com.payflow.accountservice.entity.AccountOperation;
import com.payflow.accountservice.entity.AccountStatus;
import com.payflow.accountservice.exception.AccountNotActiveException;
import com.payflow.accountservice.exception.AccountNotFoundException;
import com.payflow.accountservice.exception.InsufficientBalanceException;
import com.payflow.accountservice.repository.AccountOperationRepository;
import com.payflow.accountservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountOperationWorker {

	private final AccountRepository accountRepository;
	private final AccountOperationRepository accountOperationRepository;

	@Transactional
	public AccountOperation processOperation(String idempotencyKey, String accountNumber, BigDecimal amount,
			String operationType) {

		Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		if (account.getStatus() != AccountStatus.ACTIVE) {
			throw new AccountNotActiveException("Account is not active");
		}

		BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);

		BigDecimal newBalance;

		if ("DEBIT".equals(operationType)) {
			if (account.getBalance().compareTo(normalizedAmount) < 0) {
				throw new InsufficientBalanceException("Insufficient balance");
			}

			newBalance = account.getBalance().subtract(normalizedAmount);

		} else if ("CREDIT".equals(operationType) || "COMPENSATION".equals(operationType)) {

			newBalance = account.getBalance().add(normalizedAmount);

		} else {
			throw new IllegalArgumentException("Unsupported operation type");
		}

		account.setBalance(newBalance);
		accountRepository.save(account);

		AccountOperation operation = new AccountOperation();
		operation.setIdempotencyKey(idempotencyKey);
		operation.setOperationType(operationType);
		operation.setAccountNumber(accountNumber);
		operation.setAmount(normalizedAmount);
		operation.setResultingBalance(newBalance);

		// Flush now so a unique-key violation happens inside this worker's
		// transaction and causes the balance update to roll back.
		return accountOperationRepository.saveAndFlush(operation);
	}
}
