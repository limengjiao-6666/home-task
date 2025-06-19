package com.bank.transaction.validation;

public interface ValidationStrategy<T> {
    void validate(T input);
}






