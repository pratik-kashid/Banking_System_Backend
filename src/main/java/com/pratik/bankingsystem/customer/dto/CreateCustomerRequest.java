package com.pratik.bankingsystem.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCustomerRequest {

    @NotNull
    private Long userId;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String phone;

    @NotBlank
    private String address;
}