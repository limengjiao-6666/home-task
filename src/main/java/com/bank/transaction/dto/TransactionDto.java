package com.bank.transaction.dto;

import com.bank.transaction.model.Transaction;

public record TransactionDto(
        String accountId,
        double amount,
        Transaction.TransactionType type,
        String description
) {
}


