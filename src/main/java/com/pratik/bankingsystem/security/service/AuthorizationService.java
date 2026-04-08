package com.pratik.bankingsystem.security.service;

import com.pratik.bankingsystem.account.entity.Account;
import com.pratik.bankingsystem.account.repository.AccountRepository;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.repository.CustomerRepository;
import com.pratik.bankingsystem.user.entity.User;
import com.pratik.bankingsystem.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public Customer getAuthenticatedCustomer(String email) {
        User user = getAuthenticatedUser(email);

        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    public Account getOwnedAccount(String email, String accountNumber) {
        Customer customer = getAuthenticatedCustomer(email);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalStateException("You are not authorized to access this account");
        }

        return account;
    }

    public void validateAccountOwnership(String email, String accountNumber) {
        getOwnedAccount(email, accountNumber);
    }
}