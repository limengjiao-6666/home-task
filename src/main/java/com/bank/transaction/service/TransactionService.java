package com.bank.transaction.service;

import com.bank.transaction.dto.PageResponse;
import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.model.Transaction;

public interface TransactionService {
    Transaction createTransaction(TransactionDto dto);
    Transaction getTransaction(String id);
    Transaction updateTransaction(String id, TransactionDto dto);
    PageResponse<Transaction> listTransactions(int page, int size);
    void deleteTransaction(String id);
}