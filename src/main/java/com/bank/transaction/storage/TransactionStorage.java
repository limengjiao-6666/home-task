package com.bank.transaction.storage;

import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionStorage {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String id);
    List<Transaction> findAll(int page, int size);
    void delete(String id);
    long count();
    boolean exists(TransactionDto dto);
}