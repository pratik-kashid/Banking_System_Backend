package com.pratik.bankingsystem.customer.repository;

import com.pratik.bankingsystem.customer.entity.Customer;
import com.pratik.bankingsystem.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);
    Optional<Customer> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}