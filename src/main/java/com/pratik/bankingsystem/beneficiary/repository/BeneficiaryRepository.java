package com.pratik.bankingsystem.beneficiary.repository;

import com.pratik.bankingsystem.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndAccountNumber(Long customerId, String accountNumber);

    Optional<Beneficiary> findByIdAndCustomerId(Long id, Long customerId);
}