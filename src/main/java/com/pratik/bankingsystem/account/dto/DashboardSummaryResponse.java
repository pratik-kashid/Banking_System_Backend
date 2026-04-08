package com.pratik.bankingsystem.account.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryResponse {
    private int totalAccounts;
    private BigDecimal totalBalance;
    private long recentTransactions;
}