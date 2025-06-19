// src/main/java/com/bank/transaction/model/Transaction.java
package com.bank.transaction.model;

import java.time.LocalDateTime;

public record Transaction(
        String id,
        String accountId,
        double amount,
        TransactionType type,
        LocalDateTime timestamp,
        String description
) {
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER
    }
}

