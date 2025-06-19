package com.bank.transaction.model;

import java.time.LocalDateTime;

public record AccountStatus(
        String accountId,
        boolean isBlacklisted,
        LocalDateTime blacklistUntil
) {}
