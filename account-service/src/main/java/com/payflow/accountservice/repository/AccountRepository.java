package com.payflow.accountservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.payflow.accountservice.entity.Account;
import com.payflow.accountservice.entity.AccountType;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByAccountNumber(String accountNumber);

	List<Account> findByUserId(Long userId);

	boolean existsByAccountNumber(String accountNumber);

	boolean existsByUserIdAndAccountType(Long userId, AccountType accountType);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Account a where a.accountNumber = :accountNumber")
	Optional<Account> findByAccountNumberForUpdate(
	        @Param("accountNumber") String accountNumber);
}