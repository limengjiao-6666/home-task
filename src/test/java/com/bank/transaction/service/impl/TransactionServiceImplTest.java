package com.bank.transaction.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bank.transaction.dto.PageResponse;
import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.Transaction.TransactionType;
import com.bank.transaction.storage.TransactionStorage;
import com.bank.transaction.validation.validator.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionStorage storage;

    @Mock
    private TransactionValidator validator;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionDto validDto;
    private Transaction existingTransaction;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        validDto = new TransactionDto("acc-123", 100.0, TransactionType.DEPOSIT, "Salary");
        existingTransaction = new Transaction(
                "txn-123", "acc-123", 50.0, TransactionType.DEPOSIT,
                LocalDateTime.now(), "Old Description"
        );
    }

    // 测试创建交易
    @Test
    void createTransaction_WithValidInput_ShouldReturnSavedTransaction() {
        doNothing().when(validator).validate(validDto);
        when(storage.exists(validDto)).thenReturn(false);
        when(storage.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(validDto);

        assertNotNull(result.id());
        assertEquals(100.0, result.amount());
        verify(storage, times(1)).save(any());
    }

    @Test
    void createTransaction_WithDuplicateTransaction_ShouldThrowException() {
        doNothing().when(validator).validate(validDto);
        when(storage.exists(validDto)).thenReturn(true);
        assertThrows(BusinessException.class, () -> transactionService.createTransaction(validDto));
    }

    // 测试查询交易
    @Test
    void getTransaction_WithExistingId_ShouldReturnTransaction() {
        when(storage.findById("txn-123")).thenReturn(Optional.of(existingTransaction));
        Transaction result = transactionService.getTransaction("txn-123");
        assertEquals("txn-123", result.id());
    }

    @Test
    void getTransaction_WithNonExistentId_ShouldThrowException() {
        when(storage.findById("txn-404")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> transactionService.getTransaction("txn-404"));
    }

    // 测试更新交易
    @Test
    void updateTransaction_WithValidInput_ShouldUpdateFields() {
        when(storage.findById("txn-123")).thenReturn(Optional.of(existingTransaction));
        when(storage.exists(validDto)).thenReturn(false);
        when(storage.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.updateTransaction("txn-123", validDto);

        assertEquals("Salary", result.description());
        verify(storage, times(1)).save(any());
    }

    @Test
    void updateTransaction_WithDuplicateTransaction_ShouldThrowException() {
        when(storage.findById("txn-123")).thenReturn(Optional.of(existingTransaction));
        when(storage.exists(validDto)).thenReturn(true);
        assertThrows(BusinessException.class,
                () -> transactionService.updateTransaction("txn-123", validDto));
    }

    // 测试删除交易
//    @Test
//    void deleteTransaction_ShouldInvokeStorage() {
//        Cache mockCache = mock(Cache.class);
//        when(cacheManager.getCache("transactions")).thenReturn(mockCache);
//        doNothing().when(storage).delete("txn-123");
//
//        transactionService.deleteTransaction("txn-123");
//
//        verify(storage, times(1)).delete("txn-123");
//    }

    // 测试分页查询
    @Test
    void listTransactions_ShouldReturnPaginatedResults() {
        List<Transaction> mockList = List.of(existingTransaction);
        when(storage.findAll(1, 10)).thenReturn(mockList);
        when(storage.count()).thenReturn(1L);

        PageResponse<Transaction> result = transactionService.listTransactions(1, 10);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalPages());
    }
}
