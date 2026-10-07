package com.payflow.transactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.payflow.transactionservice.entity.TransactionStatus;
import com.payflow.transactionservice.entity.TransactionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionResponse {

    private Long id;
    private String referenceNumber;
    private Long userId;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
}