package com.bank.transaction.storage.impl;

import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.storage.TransactionStorage;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryTransactionStorage implements TransactionStorage {
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private final Set<String> transactionHashes = ConcurrentHashMap.newKeySet();

    @Override
    public Transaction save(Transaction transaction) {
        transactions.put(transaction.id(), transaction);
        transactionHashes.add(generateHash(transaction));
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public List<Transaction> findAll(int page, int size) {
        return transactions.values().stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        Transaction transaction = transactions.remove(id);
        if (transaction != null) {
            transactionHashes.remove(generateHash(transaction));
        }
    }

    @Override
    public long count() {
        return transactions.size();
    }

    @Override
    public boolean exists(TransactionDto dto) {
        String hash = generateHash(dto);
        return transactionHashes.contains(hash);
    }

    private String generateHash(Transaction transaction) {
        return generateHash(new TransactionDto(
                transaction.accountId(),
                transaction.amount(),
                transaction.type(),
                transaction.description()
        ));
    }

    private String generateHash(TransactionDto dto) {
        return DigestUtils.md5DigestAsHex(
                (dto.accountId() + dto.amount() + dto.type() + dto.description()).getBytes()
        );
    }
}