package com.pratik.bankingsystem.audit.service;

import com.pratik.bankingsystem.audit.entity.AuditLog;
import com.pratik.bankingsystem.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String userEmail, String action, String details, String ipAddress, boolean success) {
        AuditLog auditLog = AuditLog.builder()
                .userEmail(userEmail)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .success(success)
                .build();

        auditLogRepository.save(auditLog);
    }
}