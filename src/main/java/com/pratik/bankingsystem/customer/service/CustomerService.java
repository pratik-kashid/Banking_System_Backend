package com.pratik.bankingsystem.customer.service;

import com.pratik.bankingsystem.common.enums.KycStatus;
import com.pratik.bankingsystem.customer.dto.CreateCustomerRequest;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.repository.CustomerRepository;
import com.pratik.bankingsystem.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer createCustomer(User user, CreateCustomerRequest request) {
        if (customerRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException("Customer profile already exists for this user");
        }

        if (customerRepository.existsByGovernmentId(request.getGovernmentId())) {
            throw new IllegalStateException("Government ID already exists");
        }

        Customer customer = Customer.builder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .address(request.getAddress())
                .governmentId(request.getGovernmentId())
                .nomineeName(request.getNomineeName())
                .occupation(request.getOccupation())
                .kycStatus(KycStatus.PENDING)
                .build();

        return customerRepository.save(customer);
    }
}