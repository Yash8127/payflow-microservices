package com.payflow.transactionservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.transactionservice.client.AccountServiceClient;
import com.payflow.transactionservice.dto.AccountResponse;

@RestController
@RequestMapping("/transaction-test")
public class TransactionTestController {

    private final AccountServiceClient accountServiceClient;

    public TransactionTestController(
            AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @GetMapping("/account/{accountNumber}")
    public AccountResponse getAccount(
            @PathVariable String accountNumber) {

        return accountServiceClient
                .getAccountByNumber(accountNumber);
    }
    @GetMapping("/internal-account/{accountNumber}")
    public AccountResponse getInternalAccount(
            @PathVariable String accountNumber) {

        return accountServiceClient.getAccountByNumberInternal(accountNumber);
    }
}