
package com.payflow.accountservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_operations", uniqueConstraints = {
		@UniqueConstraint(name = "uk_account_operation_idempotency_key", columnNames = "idempotency_key") })
@Getter
@Setter
@NoArgsConstructor
public class AccountOperation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "idempotency_key", nullable = false, length = 150)
	private String idempotencyKey;

	@Column(name = "operation_type", nullable = false, length = 30)
	private String operationType;

	@Column(name = "account_number", nullable = false, length = 50)
	private String accountNumber;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;
	
	@Column(name = "resulting_balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal resultingBalance;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
