package com.pratik.bankingsystem.common.exception;

import com.pratik.bankingsystem.audit.service.AuditLogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditLogService auditLogService;

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        logFailure(ex.getMessage(), request);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        logFailure(ex.getMessage(), request);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        logFailure("Invalid email or password", request);
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logFailure("Validation failed", request);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        response.put("fields", fieldErrors);
        return response;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private void logFailure(String message, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : "anonymous";

        auditLogService.log(
                email,
                "API_FAILURE",
                message,
                request.getRemoteAddr(),
                false
        );
    }

    private Map<String, Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", message);
        return response;
    }

    @ExceptionHandler(CannotAcquireLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleLockException(CannotAcquireLockException ex, HttpServletRequest request) {
        logFailure("Account is busy, please retry", request);
        return buildResponse(HttpStatus.CONFLICT, "Account is busy, please retry");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleOptimisticLockException(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        logFailure("Concurrent update detected, please retry", request);
        return buildResponse(HttpStatus.CONFLICT, "Concurrent update detected, please retry");
    }
}