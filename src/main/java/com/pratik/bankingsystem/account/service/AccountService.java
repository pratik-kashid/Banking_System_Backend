package com.pratik.bankingsystem.account.service;

import com.pratik.bankingsystem.account.dto.CreateAccountRequest;
import com.pratik.bankingsystem.account.dto.DashboardSummaryResponse;
import com.pratik.bankingsystem.account.entity.Account;
import com.pratik.bankingsystem.account.repository.AccountRepository;
import com.pratik.bankingsystem.common.enums.AccountStatus;
import com.pratik.bankingsystem.common.util.BankingUtils;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.repository.CustomerRepository;
import com.pratik.bankingsystem.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public Account createAccount(CreateAccountRequest request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        return createAccountForCustomer(customer, request);
    }

    public Account createAccountForCustomer(Customer customer, CreateAccountRequest request) {
        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .currency(request.getCurrency().toUpperCase())
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public Account getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    public DashboardSummaryResponse getDashboardSummary(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long recentTransactions = accounts.stream()
                .map(Account::getId)
                .mapToLong(accountId ->
                        transactionRepository
                                .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accountId, accountId)
                                .size())
                .sum();

        return DashboardSummaryResponse.builder()
                .totalAccounts(accounts.size())
                .totalBalance(totalBalance)
                .recentTransactions(recentTransactions)
                .build();
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = BankingUtils.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}