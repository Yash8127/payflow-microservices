package com.payflow.transactionservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.transactionservice.dto.TransactionResponse;
import com.payflow.transactionservice.dto.TransferRequest;
import com.payflow.transactionservice.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@PostMapping("/transfer")
	public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {

		TransactionResponse response = transactionService.transfer(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/my")
	public ResponseEntity<List<TransactionResponse>> getMyTransactions() {

		return ResponseEntity.ok(transactionService.getMyTransactions());
	}

	@GetMapping("/{referenceNumber}")
	public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String referenceNumber) {

		return ResponseEntity.ok(transactionService.getTransactionByReference(referenceNumber));
	}

	@GetMapping("/account/{accountNumber}")
	public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable String accountNumber) {

		return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountNumber));
	}
}