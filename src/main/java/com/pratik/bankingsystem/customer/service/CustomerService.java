package com.pratik.bankingsystem.customer.service;

import com.pratik.bankingsystem.account.dto.CreateManagedAccountRequest;
import com.pratik.bankingsystem.common.enums.KycStatus;
import com.pratik.bankingsystem.common.enums.Role;
import com.pratik.bankingsystem.common.enums.UserStatus;
import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.customer.repository.CustomerRepository;
import com.pratik.bankingsystem.user.entity.User;
import com.pratik.bankingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Customer createOrUpdateCustomerForAccountOpening(CreateManagedAccountRequest request) {
        Customer existingCustomer = customerRepository.findByGovernmentId(request.getGovernmentId()).orElse(null);

        if (existingCustomer != null) {
            User linkedUser = existingCustomer.getUser();

            if (!linkedUser.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new IllegalStateException("Government ID already exists with another email");
            }

            linkedUser.setFullName(request.getFullName());
            linkedUser.setEmail(request.getEmail());
            userRepository.save(linkedUser);

            existingCustomer.setDateOfBirth(request.getDateOfBirth());
            existingCustomer.setPhone(request.getPhone());
            existingCustomer.setAddress(request.getAddress());
            existingCustomer.setGovernmentId(request.getGovernmentId());
            existingCustomer.setNomineeName(request.getNomineeName());
            existingCustomer.setOccupation(request.getOccupation());

            return customerRepository.save(existingCustomer);
        }

        User existingUserByEmail = userRepository.findByEmail(request.getEmail()).orElse(null);

        User customerUser;
        if (existingUserByEmail != null) {
            if (existingUserByEmail.getRole() != Role.CUSTOMER) {
                throw new IllegalStateException("Email already belongs to a bank staff/admin account");
            }

            if (customerRepository.existsByUserId(existingUserByEmail.getId())) {
                throw new IllegalStateException("Customer profile already exists for this email");
            }

            existingUserByEmail.setFullName(request.getFullName());
            customerUser = userRepository.save(existingUserByEmail);
        } else {
            customerUser = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode("TEMP-" + UUID.randomUUID()))
                    .role(Role.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();

            customerUser = userRepository.save(customerUser);
        }

        Customer customer = Customer.builder()
                .user(customerUser)
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