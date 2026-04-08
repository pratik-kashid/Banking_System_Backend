package com.pratik.bankingsystem.customer.controller;

import com.pratik.bankingsystem.customer.dto.CreateCustomerRequest;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public Customer createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }
}