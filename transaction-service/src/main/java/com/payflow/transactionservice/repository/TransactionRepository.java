package com.payflow.transactionservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payflow.transactionservice.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Optional<Transaction> findByReferenceNumber(String referenceNumber);

	List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Transaction> findBySourceAccountIdOrderByCreatedAtDesc(Long sourceAccountId);

	List<Transaction> findByDestinationAccountIdOrderByCreatedAtDesc(Long destinationAccountId);

	List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(Long sourceAccountId,
			Long destinationAccountId);
}