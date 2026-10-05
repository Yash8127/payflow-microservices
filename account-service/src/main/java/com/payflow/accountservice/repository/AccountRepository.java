package com.payflow.accountservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payflow.accountservice.entity.Account;
import com.payflow.accountservice.entity.AccountType;

public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByAccountNumber(String accountNumber);

	List<Account> findByUserId(Long userId);

	boolean existsByAccountNumber(String accountNumber);

	boolean existsByUserIdAndAccountType(Long userId, AccountType accountType);
}