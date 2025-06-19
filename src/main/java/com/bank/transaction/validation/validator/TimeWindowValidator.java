package com.bank.transaction.validation.validator;

import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.exception.ErrorCode;
import com.bank.transaction.validation.ValidationStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class TimeWindowValidator implements ValidationStrategy<TransactionDto> {
    private static final LocalTime START_TIME = LocalTime.of(6, 0);
    private static final LocalTime END_TIME = LocalTime.of(23, 0);

    @Override
    public void validate(TransactionDto dto) {
        LocalTime now = LocalTime.now();
        if (now.isBefore(START_TIME) || now.isAfter(END_TIME)) {
            throw new BusinessException(ErrorCode.OUTSIDE_TRANSACTION_WINDOW);
        }
    }
}