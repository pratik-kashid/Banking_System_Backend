package com.pratik.bankingsystem.customer.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCustomerRequest {

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank
    @Size(min = 10, max = 255)
    private String address;

    @NotBlank
    @Size(min = 6, max = 50)
    private String governmentId;

    @NotBlank
    @Size(min = 3, max = 100)
    private String nomineeName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String occupation;
}