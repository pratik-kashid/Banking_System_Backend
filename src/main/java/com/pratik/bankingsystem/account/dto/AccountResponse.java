package com.pratik.bankingsystem.account.dto;

import com.pratik.bankingsystem.common.enums.AccountStatus;
import com.pratik.bankingsystem.common.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
}