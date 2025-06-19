package com.bank.transaction.service.impl;

import com.bank.transaction.dto.PageResponse;
import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.exception.ErrorCode;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.service.TransactionService;
import com.bank.transaction.storage.TransactionStorage;
import com.bank.transaction.validation.validator.TransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionStorage storage;
    private final TransactionValidator validator;
    // 暂时无用，为后续魂村预热、调试等场景预留
    private final CacheManager cacheManager;

    @Autowired
    public TransactionServiceImpl(TransactionStorage storage,
                                  TransactionValidator validator,
                                  CacheManager cacheManager) {
        this.storage = storage;
        this.validator = validator;
        this.cacheManager = cacheManager;
    }

    @Override
    @CachePut(value = "transactions", key = "#result.id")
    public Transaction createTransaction(TransactionDto dto) {
        validator.validate(dto);

        if (storage.exists(dto)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSACTION);
        }

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                dto.accountId(),
                dto.amount(),
                dto.type(),
                LocalDateTime.now(),
                dto.description()
        );

        return storage.save(transaction);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id")
    public Transaction getTransaction(String id) {
        return storage.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    @Override
    @Cacheable(value = "transactions", key = "'page_' + #page + '_size_' + #size")
    public PageResponse<Transaction> listTransactions(int page, int size) {
        List<Transaction> transactions = storage.findAll(page, size);
        long total = storage.count();
        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResponse<>(transactions, page, size, total, totalPages);
    }

    @Override
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransaction(String id) {
        storage.delete(id);
    }

    @Override
    @CachePut(value = "transactions", key = "#id")
    public Transaction updateTransaction(String id, TransactionDto dto) {
        // 1. 校验业务规则
        validator.validate(dto);

        // 2. 获取现有交易记录
        Transaction existing = storage.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 3. 检查重复交易（排除自身）
        if (storage.exists(dto) && !isSameTransaction(existing, dto)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSACTION);
        }

        // 4. 构建更新后的交易记录（保留原始创建时间）
        Transaction updated = new Transaction(
                existing.id(),
                dto.accountId(),
                dto.amount(),
                dto.type(),
                existing.timestamp(),  // 保留原始创建时间
                dto.description()
        );

        // 5. 持久化存储并返回（自动更新缓存）
        return storage.save(updated);
    }

    private boolean isSameTransaction(Transaction existing, TransactionDto dto) {
        return existing.accountId().equals(dto.accountId()) &&
                existing.amount() == dto.amount() &&
                existing.type() == dto.type() &&
                existing.description().equals(dto.description());
    }
}