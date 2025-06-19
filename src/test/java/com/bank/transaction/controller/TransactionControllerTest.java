package com.bank.transaction.controller;

import com.bank.transaction.dto.PageResponse;
import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.exception.ErrorCode;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.bank.transaction.model.Transaction.TransactionType.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_ValidRequest_Returns201Created() throws Exception {
        // 准备测试数据
        TransactionDto dto = new TransactionDto("ACC-123", 500.00, DEPOSIT, "Salary");
        Transaction transaction = new Transaction("123", "ACC-123", 500.00, DEPOSIT, LocalDateTime.now(), "Salary");

        // 模拟服务层行为
        Mockito.when(transactionService.createTransaction(any(TransactionDto.class))).thenReturn(transaction);

        // 执行请求并验证
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("123")))
                .andExpect(jsonPath("$.accountId", is("ACC-123")))
                .andExpect(jsonPath("$.amount", is(500.00)))
                .andExpect(jsonPath("$.type", is("DEPOSIT")))
                .andExpect(jsonPath("$.description", is("Salary")));
    }


    @Test
    void createTransaction_DuplicateData_Returns400() throws Exception {
        // 准备测试数据
        TransactionDto dto = new TransactionDto("ACC-123", 500.00, DEPOSIT, "Salary");

        // 模拟服务层抛出重复数据异常
        Mockito.when(transactionService.createTransaction(any(TransactionDto.class)))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_TRANSACTION));

        // 执行请求并验证
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Duplicate")));
    }

    @Test
    void getTransaction_Exists_Returns200WithData() throws Exception {
        // 准备测试数据
        String transactionId = "txn-001";
        Transaction transaction = new Transaction(transactionId, "ACC-123", 100.50, WITHDRAWAL,
                LocalDateTime.now(), "Grocery");

        // 模拟服务层行为
        Mockito.when(transactionService.getTransaction(transactionId)).thenReturn(transaction);

        // 执行请求并验证
        mockMvc.perform(get("/api/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(transactionId)))
                .andExpect(jsonPath("$.accountId", is("ACC-123")))
                .andExpect(jsonPath("$.amount", is(100.50)))
                .andExpect(jsonPath("$.type", is("WITHDRAWAL")));
    }

    @Test
    void getTransaction_NotFound_Returns404() throws Exception {
        // 准备测试数据
        String nonExistentId = "non-existent-id";

        // 模拟服务层抛出找不到资源异常
        Mockito.when(transactionService.getTransaction(nonExistentId))
                .thenThrow(new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 执行请求并验证
        mockMvc.perform(get("/api/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void listTransactions_ValidRequest_ReturnsPagedResults() throws Exception {
        // 准备测试数据
        int page = 0;
        int size = 10;
        Transaction transaction = new Transaction("txn-001", "ACC-123", 200.00, DEPOSIT,
                LocalDateTime.now(), "Deposit");
        PageResponse<Transaction> response = new PageResponse<>(Collections.singletonList(transaction), page, size, 1, 1);

        // 模拟服务层行为
        Mockito.when(transactionService.listTransactions(page, size)).thenReturn(response);

        // 执行请求并验证
        mockMvc.perform(get("/api/transactions")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is("txn-001")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.page", is(page)))
                .andExpect(jsonPath("$.size", is(size)));
    }


    @Test
    void updateTransaction_Success_Returns200UpdatedData() throws Exception {
        // 准备测试数据
        String transactionId = "txn-001";
        TransactionDto dto = new TransactionDto("ACC-123", 300.00, DEPOSIT, "Updated deposit");
        Transaction updated = new Transaction(transactionId, "ACC-123", 300.00, DEPOSIT,
                LocalDateTime.now(), "Updated deposit");

        // 模拟服务层行为
        Mockito.when(transactionService.updateTransaction(eq(transactionId), any(TransactionDto.class)))
                .thenReturn(updated);

        // 执行请求并验证
        mockMvc.perform(put("/api/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(300.00)))
                .andExpect(jsonPath("$.description", is("Updated deposit")));
    }

    @Test
    void updateTransaction_NotFound_Returns404() throws Exception {
        // 准备测试数据
        String nonExistentId = "non-existent-id";
        TransactionDto dto = new TransactionDto("ACC-123", 500.00, DEPOSIT, "Salary");

        // 模拟服务层抛出找不到资源异常
        Mockito.when(transactionService.updateTransaction(eq(nonExistentId), any(TransactionDto.class)))
                .thenThrow(new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 执行请求并验证
        mockMvc.perform(put("/api/transactions/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void deleteTransaction_Success_Returns204NoContent() throws Exception {
        // 准备测试数据
        String transactionId = "txn-001";

        // 模拟服务层行为（无抛出异常表示成功）
        Mockito.doNothing().when(transactionService).deleteTransaction(transactionId);

        // 执行请求并验证
        mockMvc.perform(delete("/api/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTransaction_NotFound_Returns404() throws Exception {
        // 准备测试数据
        String nonExistentId = "non-existent-id";

        // 模拟服务层抛出找不到资源异常
        Mockito.doThrow(new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND))
                .when(transactionService).deleteTransaction(nonExistentId);

        // 执行请求并验证
        mockMvc.perform(delete("/api/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }
}