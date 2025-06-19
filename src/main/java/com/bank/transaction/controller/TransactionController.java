package com.bank.transaction.controller;

import com.bank.transaction.dto.PageResponse;
import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 交易记录控制器
 * 提供交易记录的增删改查RESTful接口
 * 路径前缀：/api/transactions
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 创建交易记录
     * @param dto 交易数据传输对象（包含账户ID、金额、类型等）
     * @return HTTP 201响应体包含新创建的交易记录
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionDto dto) {
        Transaction created = transactionService.createTransaction(dto);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * 查询单个交易记录
     * @param id 交易记录唯一标识符
     * @return HTTP 200响应体包含交易记录详情（若存在）
     * @throws BusinessException 当交易记录不存在时返回404状态码
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
        Transaction transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    /**
     * 分页查询交易记录
     * @param page 页码（从0开始）
     * @param size 每页记录数（最大100）
     * @return HTTP 200响应体包含分页数据（记录列表、总数等）
     */
    @GetMapping
    public ResponseEntity<PageResponse<Transaction>> listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Transaction> response = transactionService.listTransactions(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新交易记录
     * @param id 待更新交易记录ID
     * @param dto 更新后的交易数据
     * @return HTTP 200响应体包含更新后的完整记录
     * @throws BusinessException 当记录不存在时返回404，重复数据时返回409
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable String id,
            @RequestBody TransactionDto dto) {
        Transaction updated = transactionService.updateTransaction(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除交易记录
     * @param id 待删除记录ID
     * @return HTTP 204无内容响应（成功时）
     * @throws BusinessException 当记录不存在时返回404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
