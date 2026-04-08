package com.pratik.bankingsystem.transaction.dto;

import com.pratik.bankingsystem.common.enums.TransactionType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionFilterRequest {

    private String accountNumber;
    private TransactionType type;
    private LocalDate startDate;
    private LocalDate endDate;
}