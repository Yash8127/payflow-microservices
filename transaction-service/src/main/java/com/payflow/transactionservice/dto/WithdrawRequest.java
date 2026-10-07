package com.payflow.transactionservice.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawRequest {

    private BigDecimal amount;
}