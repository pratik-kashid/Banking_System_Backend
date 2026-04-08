package com.pratik.bankingsystem.transaction.controller;

import com.pratik.bankingsystem.audit.service.AuditLogService;
import com.pratik.bankingsystem.transaction.dto.AmountRequest;
import com.pratik.bankingsystem.transaction.dto.TransactionFilterRequest;
import com.pratik.bankingsystem.transaction.dto.TransactionResponse;
import com.pratik.bankingsystem.transaction.dto.TransferRequest;
import com.pratik.bankingsystem.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuditLogService auditLogService;

    @PostMapping("/transfer")
    public TransactionResponse transfer(Authentication authentication,
                                        HttpServletRequest httpRequest,
                                        @Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(authentication.getName(), request);

        auditLogService.log(
                authentication.getName(),
                "TRANSFER",
                "Transferred " + request.getAmount() + " from " + request.getFromAccount() + " to " + request.getToAccount(),
                httpRequest.getRemoteAddr(),
                true
        );

        return response;
    }

    @PostMapping("/deposit")
    public TransactionResponse deposit(Authentication authentication,
                                       HttpServletRequest httpRequest,
                                       @Valid @RequestBody AmountRequest request) {
        TransactionResponse response = transactionService.deposit(authentication.getName(), request);

        auditLogService.log(
                authentication.getName(),
                "DEPOSIT",
                "Deposited " + request.getAmount() + " into " + request.getAccountNumber(),
                httpRequest.getRemoteAddr(),
                true
        );

        return response;
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(Authentication authentication,
                                        HttpServletRequest httpRequest,
                                        @Valid @RequestBody AmountRequest request) {
        TransactionResponse response = transactionService.withdraw(authentication.getName(), request);

        auditLogService.log(
                authentication.getName(),
                "WITHDRAW",
                "Withdrew " + request.getAmount() + " from " + request.getAccountNumber(),
                httpRequest.getRemoteAddr(),
                true
        );

        return response;
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

    @GetMapping("/statement")
    public ResponseEntity<byte[]> downloadStatement(Authentication authentication) throws Exception {
        byte[] pdf = transactionService.generateStatement(authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}