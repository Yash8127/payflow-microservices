package com.payflow.transactionservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.payflow.transactionservice.dto.ErrorResponse;

import feign.FeignException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(FeignException.Forbidden.class)
	public ResponseEntity<ErrorResponse> handleForbidden(FeignException.Forbidden ex) {

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403, "Access denied"));
	}

	@ExceptionHandler(FeignException.NotFound.class)
	public ResponseEntity<ErrorResponse> handleNotFound(FeignException.NotFound ex) {

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, "Account not found"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		ex.printStackTrace(); // temporary debugging
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(500, "Internal server error"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {

		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
	}

	@ExceptionHandler(InvalidTransactionException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTransaction(InvalidTransactionException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	@ExceptionHandler(TransactionException.class)
	public ResponseEntity<ErrorResponse> handleTransactionException(TransactionException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}
}