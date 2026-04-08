package com.pratik.bankingsystem.common.util;

import java.math.BigDecimal;

public final class TransactionLimitRules {

    private TransactionLimitRules() {
    }

    public static final BigDecimal MAX_DEPOSIT_PER_TXN = new BigDecimal("500000.00");
    public static final BigDecimal MAX_WITHDRAW_PER_TXN = new BigDecimal("50000.00");
    public static final BigDecimal MAX_TRANSFER_PER_TXN = new BigDecimal("200000.00");
    public static final BigDecimal MIN_BALANCE = new BigDecimal("0.00");
}