package com.payflow.accountservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.payflow.accountservice.dto.AccountResponse;
import com.payflow.accountservice.dto.InternalAmountRequest;
import com.payflow.accountservice.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

	private final AccountService accountService;

	@PostMapping("/{accountNumber}/debit")
	public ResponseEntity<AccountResponse> debit(@PathVariable String accountNumber,
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@Valid @RequestBody InternalAmountRequest request) {

		return ResponseEntity.ok(accountService.debitInternal(accountNumber, request.getAmount(), idempotencyKey));
	}

	@PostMapping("/{accountNumber}/credit")
	public ResponseEntity<AccountResponse> credit(@PathVariable String accountNumber,
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@RequestParam(defaultValue = "CREDIT") String operationType,
			@Valid @RequestBody InternalAmountRequest request) {

		return ResponseEntity
				.ok(accountService.creditInternal(accountNumber, request.getAmount(), idempotencyKey, operationType));
	}

	@GetMapping("/number/{accountNumber}")
	public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {

		return ResponseEntity.ok(accountService.getAccountByNumberInternal(accountNumber));
	}
}