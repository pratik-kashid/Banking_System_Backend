package com.pratik.bankingsystem.beneficiary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeneficiaryResponse {

    private Long id;
    private String accountNumber;
    private String name;
    private String nickname;
}