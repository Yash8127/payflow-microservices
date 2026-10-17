
package com.payflow.accountservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payflow.accountservice.entity.AccountOperation;

public interface AccountOperationRepository
        extends JpaRepository<AccountOperation, Long> {

    Optional<AccountOperation> findByIdempotencyKey(String idempotencyKey);
}
