package com.payflow.accountservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.accountservice.dto.AccountResponse;
import com.payflow.accountservice.dto.CreateAccountRequest;
import com.payflow.accountservice.dto.DepositRequest;
import com.payflow.accountservice.dto.WithdrawRequest;
import com.payflow.accountservice.entity.Account;
import com.payflow.accountservice.entity.AccountStatus;
import com.payflow.accountservice.exception.AccountAlreadyExistsException;
import com.payflow.accountservice.exception.AccountNotActiveException;
import com.payflow.accountservice.exception.AccountNotFoundException;
import com.payflow.accountservice.exception.InsufficientBalanceException;
import com.payflow.accountservice.repository.AccountRepository;

@Service
public class AccountService {

	private final AccountRepository accountRepository;

	private final SecureRandom secureRandom = new SecureRandom();

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	public AccountResponse createAccount(CreateAccountRequest request) {

		Long authenticatedUserId = getAuthenticatedUserId();

		boolean accountExists = accountRepository.existsByUserIdAndAccountType(authenticatedUserId,
				request.getAccountType());

		if (accountExists) {
			throw new AccountAlreadyExistsException("User already has a " + request.getAccountType() + " account");
		}

		Account account = new Account();

		account.setUserId(authenticatedUserId);
		account.setAccountType(request.getAccountType());
		account.setAccountNumber(generateAccountNumber());
		account.setBalance(BigDecimal.ZERO);

		Account savedAccount = accountRepository.save(account);

		return mapToResponse(savedAccount);
	}

	public AccountResponse getAccountById(Long id) {

		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		Long authenticatedUserId = getAuthenticatedUserId();

		validateOwnership(account, authenticatedUserId);

		return mapToResponse(account);
	}

	public AccountResponse getAccountByNumber(String accountNumber) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		Long authenticatedUserId = getAuthenticatedUserId();

		validateOwnership(account, authenticatedUserId);

		return mapToResponse(account);
	}

	public List<AccountResponse> getAccountsByUserId(Long userId) {

		Long authenticatedUserId = getAuthenticatedUserId();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin && !userId.equals(authenticatedUserId)) {
			throw new AccessDeniedException("You do not have access to these accounts");
		}

		return accountRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
	}

	private String generateAccountNumber() {

		String accountNumber;

		do {
			long number = 1000000000L + secureRandom.nextLong(9000000000L);

			accountNumber = "PF" + number;

		} while (accountRepository.existsByAccountNumber(accountNumber));

		return accountNumber;
	}

	private void validateOwnership(Account account, Long authenticatedUserId) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

		if (isAdmin) {
			return;
		}

		if (!account.getUserId().equals(authenticatedUserId)) {
			throw new AccessDeniedException("You do not have access to this account");
		}
	}

	private Long getAuthenticatedUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		return (Long) authentication.getPrincipal();
	}

	@Transactional
	public AccountResponse deposit(String accountNumber, DepositRequest request) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		Long authenticatedUserId = getAuthenticatedUserId();

		validateOwnership(account, authenticatedUserId);

		if (account.getStatus() != AccountStatus.ACTIVE) {
			throw new AccountNotActiveException("Account is not active");
		}
		BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);

		account.setBalance(account.getBalance().add(amount));

		Account updatedAccount = accountRepository.save(account);

		return mapToResponse(updatedAccount);
	}

	@Transactional
	public AccountResponse withdraw(String accountNumber, WithdrawRequest request) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		Long authenticatedUserId = getAuthenticatedUserId();

		validateOwnership(account, authenticatedUserId);

		if (account.getStatus() != AccountStatus.ACTIVE) {
			throw new AccountNotActiveException("Account is not active");
		}
		BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);

		if (account.getBalance().compareTo(amount) < 0) {
			throw new InsufficientBalanceException("Insufficient balance");
		}
		account.setBalance(account.getBalance().subtract(amount));

		Account updatedAccount = accountRepository.save(account);

		return mapToResponse(updatedAccount);
	}

	public AccountResponse freezeAccount(Long id) {

		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		account.setStatus(AccountStatus.FROZEN);

		Account updatedAccount = accountRepository.save(account);

		return mapToResponse(updatedAccount);
	}

	public AccountResponse unfreezeAccount(Long id) {

		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		account.setStatus(AccountStatus.ACTIVE);

		Account updatedAccount = accountRepository.save(account);

		return mapToResponse(updatedAccount);
	}
	//INTERNAL OR METHODS RELATED TO COMMUNICAION BETWEEN TRANSCATION AND ACCOUNT SERVICES

	@Transactional
	public AccountResponse debitInternal(String accountNumber, BigDecimal amount) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		if (account.getStatus() != AccountStatus.ACTIVE) {
			throw new AccountNotActiveException("Account is not active");
		}

		amount = amount.setScale(2, RoundingMode.HALF_UP);

		if (account.getBalance().compareTo(amount) < 0) {
			throw new InsufficientBalanceException("Insufficient balance");
		}

		account.setBalance(account.getBalance().subtract(amount));

		Account updatedAccount = accountRepository.save(account);

		return mapToResponse(updatedAccount);
	}

	@Transactional
	public AccountResponse creditInternal(String accountNumber, BigDecimal amount) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));

		if (account.getStatus() != AccountStatus.ACTIVE) {
			throw new AccountNotActiveException("Account is not active");
		}

		amount = amount.setScale(2, RoundingMode.HALF_UP);

		account.setBalance(account.getBalance().add(amount));

		Account updatedAccount = accountRepository.save(account);

		return mapToResponse(updatedAccount);
	}
	public AccountResponse getAccountByNumberInternal(String accountNumber) {

	    Account account = accountRepository.findByAccountNumber(accountNumber)
	            .orElseThrow(() ->
	                    new AccountNotFoundException("Account not found"));

	    return mapToResponse(account);
	}

	private AccountResponse mapToResponse(Account account) {

		AccountResponse response = new AccountResponse();

		response.setId(account.getId());
		response.setUserId(account.getUserId());
		response.setAccountNumber(account.getAccountNumber());
		response.setAccountType(account.getAccountType());
		response.setBalance(account.getBalance());
		response.setStatus(account.getStatus());
		response.setCreatedAt(account.getCreatedAt());
		response.setUpdatedAt(account.getUpdatedAt());

		return response;
	}
}