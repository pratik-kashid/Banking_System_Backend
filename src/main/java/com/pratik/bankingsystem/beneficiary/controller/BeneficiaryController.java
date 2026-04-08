package com.pratik.bankingsystem.beneficiary.controller;

import com.pratik.bankingsystem.beneficiary.dto.BeneficiaryRequest;
import com.pratik.bankingsystem.beneficiary.dto.BeneficiaryResponse;
import com.pratik.bankingsystem.beneficiary.service.BeneficiaryService;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.repository.CustomerRepository;
import com.pratik.bankingsystem.user.entity.User;
import com.pratik.bankingsystem.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService service;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    private Customer getCustomer(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    @PostMapping
    public BeneficiaryResponse add(Authentication auth,
                                   @Valid @RequestBody BeneficiaryRequest request) {
        return service.addBeneficiary(getCustomer(auth), request);
    }

    @GetMapping
    public List<BeneficiaryResponse> getAll(Authentication auth) {
        return service.getCustomerBeneficiaries(getCustomer(auth).getId());
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication auth, @PathVariable Long id) {
        service.deleteBeneficiary(id, getCustomer(auth).getId());
    }
}