package com.pratik.bankingsystem.beneficiary.service;

import com.pratik.bankingsystem.account.repository.AccountRepository;
import com.pratik.bankingsystem.beneficiary.dto.BeneficiaryRequest;
import com.pratik.bankingsystem.beneficiary.dto.BeneficiaryResponse;
import com.pratik.bankingsystem.beneficiary.entity.Beneficiary;
import com.pratik.bankingsystem.beneficiary.repository.BeneficiaryRepository;
import com.pratik.bankingsystem.customer.entity.Customer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository repository;
    private final AccountRepository accountRepository;

    public BeneficiaryResponse addBeneficiary(Customer customer, BeneficiaryRequest request) {

        // check account exists
        accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account does not exist"));

        // prevent duplicate beneficiary
        if (repository.existsByCustomerIdAndAccountNumber(customer.getId(), request.getAccountNumber())) {
            throw new IllegalStateException("Beneficiary already exists");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .accountNumber(request.getAccountNumber())
                .name(request.getName())
                .nickname(request.getNickname())
                .customer(customer)
                .build();

        Beneficiary saved = repository.save(beneficiary);

        return mapToResponse(saved);
    }

    public List<BeneficiaryResponse> getCustomerBeneficiaries(Long customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteBeneficiary(Long beneficiaryId, Long customerId) {
        Beneficiary b = repository.findByIdAndCustomerId(beneficiaryId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiary not found"));

        repository.delete(b);
    }

    private BeneficiaryResponse mapToResponse(Beneficiary b) {
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .accountNumber(b.getAccountNumber())
                .name(b.getName())
                .nickname(b.getNickname())
                .build();
    }
}