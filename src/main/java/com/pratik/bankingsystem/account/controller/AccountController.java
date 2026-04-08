package com.pratik.bankingsystem.account.controller;

import com.pratik.bankingsystem.account.dto.AccountResponse;
import com.pratik.bankingsystem.account.dto.CreateAccountRequest;
import com.pratik.bankingsystem.account.dto.CreateAccountWithProfileRequest;
import com.pratik.bankingsystem.account.dto.DashboardSummaryResponse;
import com.pratik.bankingsystem.account.entity.Account;
import com.pratik.bankingsystem.account.service.AccountService;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.repository.CustomerRepository;
import com.pratik.bankingsystem.customer.service.CustomerService;
import com.pratik.bankingsystem.user.entity.User;
import com.pratik.bankingsystem.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @PostMapping("/me")
    public AccountResponse createMyAccount(Authentication authentication,
                                           @Valid @RequestBody CreateAccountWithProfileRequest request) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Customer customer = customerService.createOrUpdateCustomer(user, request);

        CreateAccountRequest accountRequest = new CreateAccountRequest();
        accountRequest.setAccountType(request.getAccountType());
        accountRequest.setCurrency(request.getCurrency());

        Account account = accountService.createAccountForCustomer(customer, accountRequest);
        return mapToResponse(account);
    }

    @GetMapping("/customer/{customerId}")
    public List<AccountResponse> getCustomerAccounts(@PathVariable Long customerId) {
        return accountService.getAccountsByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccountByNumber(@PathVariable String accountNumber) {
        return mapToResponse(accountService.getByAccountNumber(accountNumber));
    }

    @GetMapping("/me")
    public List<AccountResponse> getMyAccounts(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        return accountService.getAccountsByCustomerId(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/me/summary")
    public DashboardSummaryResponse getMyDashboardSummary(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        return accountService.getDashboardSummary(customer.getId());
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .customerName(account.getCustomer().getUser().getFullName())
                .phone(account.getCustomer().getPhone())
                .nomineeName(account.getCustomer().getNomineeName())
                .build();
    }
}