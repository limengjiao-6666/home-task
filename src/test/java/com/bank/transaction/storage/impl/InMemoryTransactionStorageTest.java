package com.bank.transaction.storage.impl;

import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.Transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionStorageTest {
    private InMemoryTransactionStorage storage;
    private Transaction sampleTransaction;
    private TransactionDto sampleDto;

    @BeforeEach
    void setUp() {
        storage = new InMemoryTransactionStorage();
        sampleTransaction = new Transaction(
                "txn-123", "acc-123", 100.0, TransactionType.DEPOSIT,
                null, "Salary"
        );
        sampleDto = new TransactionDto(
                "acc-123", 100.0, TransactionType.DEPOSIT, "Salary"
        );
    }

    // 测试保存交易记录
    @Test
    void save_ShouldStoreTransactionAndHash() {
        Transaction saved = storage.save(sampleTransaction);
        assertEquals(sampleTransaction, saved);
        assertTrue(storage.findById("txn-123").isPresent());
        assertTrue(storage.exists(sampleDto)); // 验证哈希存储
    }

    // 测试查询存在的交易记录
    @Test
    void findById_WithExistingId_ShouldReturnTransaction() {
        storage.save(sampleTransaction);
        Optional<Transaction> result = storage.findById("txn-123");
        assertTrue(result.isPresent());
        assertEquals("acc-123", result.get().accountId());
    }

    // 测试查询不存在的交易记录
    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        Optional<Transaction> result = storage.findById("txn-404");
        assertFalse(result.isPresent());
    }

    // 测试分页查询
    @Test
    void findAll_ShouldReturnPaginatedResults() {
        // 插入多条测试数据
        for (int i = 1; i <= 5; i++) {
            storage.save(new Transaction("txn-" + i, "acc-" + i, 10.0 * i, TransactionType.TRANSFER, null, "Test"));
        }
        List<Transaction> page1 = storage.findAll(0, 2);
        assertEquals(2, page1.size());

        List<Transaction> page2 = storage.findAll(1, 2);
        assertEquals(2, page2.size());

        assertNotEquals(page1.get(0).id(), page2.get(0).id());
        assertNotEquals(page1.get(1).id(), page2.get(0).id());
    }

    // 测试删除交易记录
    @Test
    void delete_ShouldRemoveTransactionAndHash() {
        storage.save(sampleTransaction);
        storage.delete("txn-123");
        assertFalse(storage.findById("txn-123").isPresent());
        assertFalse(storage.exists(sampleDto)); // 哈希应同步删除
    }

    // 测试记录计数
    @Test
    void count_ShouldReturnCorrectSize() {
        storage.save(sampleTransaction);
        assertEquals(1, storage.count());
        storage.save(new Transaction("txn-456", "acc-456", 200.0, TransactionType.WITHDRAWAL, null, "Withdraw"));
        assertEquals(2, storage.count());
    }

    // 测试哈希去重功能
    @Test
    void exists_ShouldDetectDuplicateHashes() {
        storage.save(sampleTransaction);
        assertTrue(storage.exists(sampleDto)); // 相同DTO应返回true

        TransactionDto uniqueDto = new TransactionDto("acc-999", 999.0, TransactionType.DEPOSIT, "Unique");
        assertFalse(storage.exists(uniqueDto)); // 不同DTO应返回false
    }

}
