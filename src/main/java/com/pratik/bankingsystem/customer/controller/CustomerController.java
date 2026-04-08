package com.pratik.bankingsystem.customer.controller;

import com.pratik.bankingsystem.customer.dto.CreateCustomerRequest;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.service.CustomerService;
import com.pratik.bankingsystem.user.entity.User;
import com.pratik.bankingsystem.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;

    @PostMapping("/me")
    public Customer createMyProfile(Authentication authentication,
                                    @Valid @RequestBody CreateCustomerRequest request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return customerService.createCustomer(user, request);
    }
}