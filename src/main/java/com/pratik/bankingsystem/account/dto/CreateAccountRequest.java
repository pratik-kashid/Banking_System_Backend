package com.pratik.bankingsystem.account.dto;

import com.pratik.bankingsystem.common.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull
    private AccountType accountType;

    @NotBlank
    private String currency;
}