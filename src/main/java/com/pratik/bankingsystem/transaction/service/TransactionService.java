package com.pratik.bankingsystem.transaction.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.pratik.bankingsystem.account.entity.Account;
import com.pratik.bankingsystem.account.repository.AccountRepository;
import com.pratik.bankingsystem.audit.service.AuditLogService;
import com.pratik.bankingsystem.common.enums.AccountStatus;
import com.pratik.bankingsystem.common.enums.TransactionStatus;
import com.pratik.bankingsystem.common.enums.TransactionType;
import com.pratik.bankingsystem.common.util.BankingUtils;
import com.pratik.bankingsystem.common.util.TransactionLimitRules;
import com.pratik.bankingsystem.security.service.AuthorizationService;
import com.pratik.bankingsystem.transaction.dto.AmountRequest;
import com.pratik.bankingsystem.transaction.dto.TransactionFilterRequest;
import com.pratik.bankingsystem.transaction.dto.TransactionResponse;
import com.pratik.bankingsystem.transaction.dto.TransferRequest;
import com.pratik.bankingsystem.transaction.entity.Transaction;
import com.pratik.bankingsystem.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;

    @Transactional(timeout = 5)
    public TransactionResponse transfer(String email, TransferRequest request) {
        validateAmount(request.getAmount());
        validateTransferLimit(request.getAmount());

        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new IllegalStateException("Source and destination accounts cannot be same");
        }

        authorizationService.validateAccountOwnership(email, request.getFromAccount());

        String firstLock = request.getFromAccount().compareTo(request.getToAccount()) < 0
                ? request.getFromAccount() : request.getToAccount();
        String secondLock = request.getFromAccount().compareTo(request.getToAccount()) < 0
                ? request.getToAccount() : request.getFromAccount();

        Account lockedFirst = accountRepository.findByAccountNumberForUpdate(firstLock)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + firstLock));

        Account lockedSecond = accountRepository.findByAccountNumberForUpdate(secondLock)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + secondLock));

        Account from = lockedFirst.getAccountNumber().equals(request.getFromAccount()) ? lockedFirst : lockedSecond;
        Account to = lockedFirst.getAccountNumber().equals(request.getToAccount()) ? lockedFirst : lockedSecond;

        validateActiveAccount(from);
        validateActiveAccount(to);
        validateSameCurrency(from, to);

        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        BigDecimal newSourceBalance = from.getBalance().subtract(request.getAmount());
        if (newSourceBalance.compareTo(TransactionLimitRules.MIN_BALANCE) < 0) {
            throw new IllegalStateException("Minimum balance violation");
        }

        from.setBalance(newSourceBalance);
        to.setBalance(to.getBalance().add(request.getAmount()));

        Transaction txn = Transaction.builder()
                .referenceNumber(BankingUtils.generateTransactionReference())
                .fromAccount(from)
                .toAccount(to)
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(txn);
        return mapToResponse(saved);
    }

    @Transactional(timeout = 5)
    public TransactionResponse deposit(String email, AmountRequest request) {
        validateAmount(request.getAmount());
        validateDepositLimit(request.getAmount());

        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (!authorizationService.getOwnedAccount(email, request.getAccountNumber()).getId().equals(account.getId())) {
            throw new IllegalStateException("You are not authorized to access this account");
        }

        validateActiveAccount(account);

        account.setBalance(account.getBalance().add(request.getAmount()));

        Transaction txn = Transaction.builder()
                .referenceNumber(BankingUtils.generateTransactionReference())
                .toAccount(account)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(txn);
        return mapToResponse(saved);
    }

    @Transactional(timeout = 5)
    public TransactionResponse withdraw(String email, AmountRequest request) {
        validateAmount(request.getAmount());
        validateWithdrawLimit(request.getAmount());

        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (!authorizationService.getOwnedAccount(email, request.getAccountNumber()).getId().equals(account.getId())) {
            throw new IllegalStateException("You are not authorized to access this account");
        }

        validateActiveAccount(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        if (newBalance.compareTo(TransactionLimitRules.MIN_BALANCE) < 0) {
            throw new IllegalStateException("Minimum balance violation");
        }

        account.setBalance(newBalance);

        Transaction txn = Transaction.builder()
                .referenceNumber(BankingUtils.generateTransactionReference())
                .fromAccount(account)
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(txn);
        return mapToResponse(saved);
    }

    public List<TransactionResponse> getMyAccountTransactions(String email, String accountNumber) {
        Account account = authorizationService.getOwnedAccount(email, accountNumber);

        return transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(account.getId(), account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TransactionResponse> search(String email, TransactionFilterRequest request) {
        if (request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) {
            authorizationService.validateAccountOwnership(email, request.getAccountNumber());
        }

        List<Transaction> list = transactionRepository.searchTransactions(
                request.getAccountNumber(),
                request.getType(),
                request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null,
                request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null
        );

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public byte[] generateStatement(String email) throws Exception {
        List<TransactionResponse> transactions = search(email, new TransactionFilterRequest());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Bank Statement"));
        document.add(new Paragraph("Generated On: " + java.time.LocalDateTime.now()));
        document.add(new Paragraph(" "));

        for (TransactionResponse t : transactions) {
            document.add(new Paragraph(
                    "Ref: " + t.getReferenceNumber()
                            + " | Type: " + t.getType()
                            + " | Amount: ₹" + t.getAmount()
                            + " | Date: " + t.getCreatedAt()
            ));
        }

        document.close();

        return out.toByteArray();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Amount must be greater than zero");
        }
    }

    private void validateDepositLimit(BigDecimal amount) {
        if (amount.compareTo(TransactionLimitRules.MAX_DEPOSIT_PER_TXN) > 0) {
            throw new IllegalStateException("Deposit limit exceeded");
        }
    }

    private void validateWithdrawLimit(BigDecimal amount) {
        if (amount.compareTo(TransactionLimitRules.MAX_WITHDRAW_PER_TXN) > 0) {
            throw new IllegalStateException("Withdraw limit exceeded");
        }
    }

    private void validateTransferLimit(BigDecimal amount) {
        if (amount.compareTo(TransactionLimitRules.MAX_TRANSFER_PER_TXN) > 0) {
            throw new IllegalStateException("Transfer limit exceeded");
        }
    }

    private void validateActiveAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }
    }

    private void validateSameCurrency(Account from, Account to) {
        if (!from.getCurrency().equalsIgnoreCase(to.getCurrency())) {
            throw new IllegalStateException("Cross-currency transfer is not supported");
        }
    }

    private TransactionResponse mapToResponse(Transaction txn) {
        return TransactionResponse.builder()
                .referenceNumber(txn.getReferenceNumber())
                .fromAccount(txn.getFromAccount() != null ? txn.getFromAccount().getAccountNumber() : null)
                .toAccount(txn.getToAccount() != null ? txn.getToAccount().getAccountNumber() : null)
                .amount(txn.getAmount())
                .type(txn.getType())
                .status(txn.getStatus())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}