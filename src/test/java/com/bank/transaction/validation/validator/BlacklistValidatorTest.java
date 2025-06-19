package com.bank.transaction.validation.validator;

import com.bank.transaction.dto.TransactionDto;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.exception.ErrorCode;
import com.bank.transaction.model.AccountStatus;
import com.bank.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BlacklistValidatorTest {
    @InjectMocks
    private BlacklistValidator validator;

    // 测试正常场景：账户不在黑名单中
    @Test
    void validate_WhenAccountNotBlacklisted_ShouldPass() {
        TransactionDto dto = new TransactionDto("VALID_ACCOUNT", 100.0, Transaction.TransactionType.DEPOSIT, "Salary");
        assertDoesNotThrow(() -> validator.validate(dto)); // 不应抛出异常
    }

    // 测试异常场景：账户在黑名单中
    @Test
    void validate_WhenAccountBlacklisted_ShouldThrowBusinessException() {
        TransactionDto dto = new TransactionDto("BLACKLISTED_ACCOUNT", 100.0, Transaction.TransactionType.DEPOSIT, "Salary");
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validate(dto));
        assertEquals(ErrorCode.ACCOUNT_BLACKLISTED, exception.getErrorCode()); // 验证错误码
    }

    // 测试边界条件：账户ID为null
    @Test
    void validate_WhenAccountIdIsNull_ShouldPass() {
        TransactionDto dto = new TransactionDto(null, 100.0, Transaction.TransactionType.DEPOSIT, "Salary");
        assertThrows(NullPointerException.class, () ->validator.validate(dto));
    }
}
