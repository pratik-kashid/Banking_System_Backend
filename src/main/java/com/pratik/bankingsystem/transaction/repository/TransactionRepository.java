package com.pratik.bankingsystem.transaction.repository;

import com.pratik.bankingsystem.common.enums.TransactionType;
import com.pratik.bankingsystem.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(Long fromAccountId, Long toAccountId);

    @Query("""
           select coalesce(sum(t.amount), 0)
           from Transaction t
           where t.type = :type
             and t.status = com.pratik.bankingsystem.common.enums.TransactionStatus.SUCCESS
             and t.createdAt >= :startOfDay
             and (
                  (t.fromAccount is not null and t.fromAccount.id = :accountId)
                  or
                  (t.toAccount is not null and t.toAccount.id = :accountId)
             )
           """)
    BigDecimal sumSuccessfulAmountByAccountAndTypeSince(@Param("accountId") Long accountId,
                                                        @Param("type") TransactionType type,
                                                        @Param("startOfDay") LocalDateTime startOfDay);

    @Query("""
           select t from Transaction t
           where (
                    :accountNumber is null
                    or :accountNumber = ''
                    or (t.fromAccount is not null and t.fromAccount.accountNumber = :accountNumber)
                    or (t.toAccount is not null and t.toAccount.accountNumber = :accountNumber)
                 )
             and (:type is null or t.type = :type)
             and (:startDate is null or t.createdAt >= :startDate)
             and (:endDate is null or t.createdAt <= :endDate)
           order by t.createdAt desc
           """)
    List<Transaction> searchTransactions(@Param("accountNumber") String accountNumber,
                                         @Param("type") TransactionType type,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}