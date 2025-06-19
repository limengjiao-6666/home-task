package com.bank.transaction.validation.validator;

import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.exception.ErrorCode;
import com.bank.transaction.model.AccountStatus;
import com.bank.transaction.validation.ValidationStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BlacklistValidator implements ValidationStrategy<TransactionDto> {
    private final Map<String, AccountStatus> accountStatusMap = Map.of(
            "BLACKLISTED_ACCOUNT", new AccountStatus("BLACKLISTED_ACCOUNT", true, null)
    );

    @Override
    public void validate(TransactionDto dto) {
        AccountStatus status = accountStatusMap.get(dto.accountId());
        if (status != null && status.isBlacklisted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_BLACKLISTED);
        }
    }
}
