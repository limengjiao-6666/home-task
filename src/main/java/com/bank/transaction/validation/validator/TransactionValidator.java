package com.bank.transaction.validation.validator;

import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.validation.ValidationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionValidator {
    private final List<ValidationStrategy<TransactionDto>> strategies;

    @Autowired
    public TransactionValidator(List<ValidationStrategy<TransactionDto>> strategies) {
        this.strategies = strategies;
    }

    public void validate(TransactionDto dto) {
        strategies.forEach(strategy -> strategy.validate(dto));
    }
}
