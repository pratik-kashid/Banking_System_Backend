package com.pratik.bankingsystem.transaction.controller;

import com.pratik.bankingsystem.transaction.dto.AmountRequest;
import com.pratik.bankingsystem.transaction.dto.TransactionFilterRequest;
import com.pratik.bankingsystem.transaction.dto.TransactionResponse;
import com.pratik.bankingsystem.transaction.dto.TransferRequest;
import com.pratik.bankingsystem.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public TransactionResponse transfer(Authentication authentication,
                                        @Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(authentication.getName(), request);
    }

    @PostMapping("/deposit")
    public TransactionResponse deposit(Authentication authentication,
                                       @Valid @RequestBody AmountRequest request) {
        return transactionService.deposit(authentication.getName(), request);
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(Authentication authentication,
                                        @Valid @RequestBody AmountRequest request) {
        return transactionService.withdraw(authentication.getName(), request);
    }

    @GetMapping("/me/{accountNumber}")
    public List<TransactionResponse> getMyAccountTransactions(Authentication authentication,
                                                              @PathVariable String accountNumber) {
        return transactionService.getMyAccountTransactions(authentication.getName(), accountNumber);
    }

    @PostMapping("/search")
    public List<TransactionResponse> search(Authentication authentication,
                                            @RequestBody TransactionFilterRequest request) {
        return transactionService.search(authentication.getName(), request);
    }
}