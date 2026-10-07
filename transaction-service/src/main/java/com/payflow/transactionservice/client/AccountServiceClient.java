package com.payflow.transactionservice.client;

import com.payflow.transactionservice.config.FeignConfig;
import com.payflow.transactionservice.dto.AccountResponse;
import com.payflow.transactionservice.dto.DepositRequest;
import com.payflow.transactionservice.dto.InternalAmountRequest;
import com.payflow.transactionservice.dto.WithdrawRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "account-service",
        configuration = FeignConfig.class
)
public interface AccountServiceClient {

    // Normal user-facing Account Service APIs
    @GetMapping("/accounts/number/{accountNumber}")
    AccountResponse getAccountByNumber(
            @PathVariable("accountNumber") String accountNumber);

    @PostMapping("/accounts/{accountNumber}/withdraw")
    AccountResponse withdraw(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody WithdrawRequest request);

    @PostMapping("/accounts/{accountNumber}/deposit")
    AccountResponse deposit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody DepositRequest request);


    // Internal service-to-service APIs
    @GetMapping("/internal/accounts/number/{accountNumber}")
    AccountResponse getAccountByNumberInternal(
            @PathVariable("accountNumber") String accountNumber);

    @PostMapping("/internal/accounts/{accountNumber}/debit")
    AccountResponse debitInternal(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody InternalAmountRequest request);

    @PostMapping("/internal/accounts/{accountNumber}/credit")
    AccountResponse creditInternal(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody InternalAmountRequest request);
}