package com.pratik.bankingsystem.transaction.service;

import com.pratik.bankingsystem.account.entity.Account;
import com.pratik.bankingsystem.account.repository.AccountRepository;
import com.pratik.bankingsystem.common.enums.AccountStatus;
import com.pratik.bankingsystem.common.enums.TransactionStatus;
import com.pratik.bankingsystem.common.enums.TransactionType;
import com.pratik.bankingsystem.common.util.BankingUtils;
import com.pratik.bankingsystem.common.util.TransactionLimitRules;
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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(timeout = 5)
    public TransactionResponse transfer(String email, TransferRequest request) {
        validateAmount(request.getAmount());
        validateTransferLimit(request.getAmount());

        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new IllegalStateException("Source and destination accounts cannot be same");
        }

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
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        return transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(account.getId(), account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TransactionResponse> search(String email, TransactionFilterRequest request) {
        if (request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) {
            Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                    .orElseThrow(() -> new EntityNotFoundException("Account not found"));

            return transactionRepository
                    .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(account.getId(), account.getId())
                    .stream()
                    .filter(txn -> request.getType() == null || txn.getType() == request.getType())
                    .filter(txn -> request.getStartDate() == null || !txn.getCreatedAt().toLocalDate().isBefore(request.getStartDate()))
                    .filter(txn -> request.getEndDate() == null || !txn.getCreatedAt().toLocalDate().isAfter(request.getEndDate()))
                    .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                    .map(this::mapToResponse)
                    .toList();
        }

        return transactionRepository.findAll()
                .stream()
                .filter(txn -> request.getType() == null || txn.getType() == request.getType())
                .filter(txn -> request.getStartDate() == null || !txn.getCreatedAt().toLocalDate().isBefore(request.getStartDate()))
                .filter(txn -> request.getEndDate() == null || !txn.getCreatedAt().toLocalDate().isAfter(request.getEndDate()))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(this::mapToResponse)
                .toList();
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