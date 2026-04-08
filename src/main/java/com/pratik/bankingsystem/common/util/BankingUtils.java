package com.pratik.bankingsystem.common.util;

import java.util.UUID;

public final class BankingUtils {

    private BankingUtils() {
    }

    public static String generateAccountNumber() {
        long number = 1000000000L + (long) (Math.random() * 9000000000L);
        return String.valueOf(number);
    }

    public static String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}