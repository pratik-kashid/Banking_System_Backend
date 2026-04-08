package com.pratik.bankingsystem.customer.service;

import com.pratik.bankingsystem.common.enums.KycStatus;
import com.pratik.bankingsystem.customer.dto.CreateCustomerRequest;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.repository.CustomerRepository;
import com.pratik.bankingsystem.user.entity.User;
import com.pratik.bankingsystem.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public Customer createCustomer(CreateCustomerRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (customerRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException("Customer profile already exists for this user");
        }

        Customer customer = Customer.builder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .address(request.getAddress())
                .kycStatus(KycStatus.PENDING)
                .build();

        return customerRepository.save(customer);
    }
}