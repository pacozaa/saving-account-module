package com.banking.transaction.repository;

import com.banking.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByAccountIdOrderByTimestampDesc(Long accountId);
    
    List<Transaction> findByAccountIdOrRelatedAccountIdOrderByTimestampDesc(Long accountId, Long relatedAccountId);
}
