package com.pratik.bankingsystem.beneficiary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BeneficiaryRequest {

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;
}