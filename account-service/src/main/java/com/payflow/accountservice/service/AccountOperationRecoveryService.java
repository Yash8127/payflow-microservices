
package com.payflow.accountservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.accountservice.entity.AccountOperation;
import com.payflow.accountservice.repository.AccountOperationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountOperationRecoveryService {

    private final AccountOperationRepository accountOperationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public AccountOperation findExistingOperation(String idempotencyKey) {
        return accountOperationRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);
    }
}
