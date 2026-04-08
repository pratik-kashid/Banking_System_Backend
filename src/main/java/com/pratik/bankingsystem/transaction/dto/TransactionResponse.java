package com.pratik.bankingsystem.transaction.dto;

import com.pratik.bankingsystem.common.enums.TransactionStatus;
import com.pratik.bankingsystem.common.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private String referenceNumber;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
}