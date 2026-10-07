package com.payflow.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponse {

	private Long id;
	private Long userId;
	private String accountNumber;
	private AccountType accountType;
	private BigDecimal balance;
	private AccountStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}