package com.bank.transaction.exception;

/**
 * 错误码枚举类，定义系统所有业务错误类型
 */
public enum ErrorCode {
    // 交易相关错误
    DUPLICATE_TRANSACTION("TRANSACTION_001", "Duplicate transaction detected"),
    TRANSACTION_NOT_FOUND("TRANSACTION_002", "Transaction not found"),

    // 验证相关错误
    ACCOUNT_BLACKLISTED("VALIDATION_001", "Account is blacklisted"),
    OUTSIDE_TRANSACTION_WINDOW("VALIDATION_002", "Transaction outside allowed time window (8:00-22:00)"),
    INVALID_TRANSACTION_DATA("VALIDATION_003", "Invalid transaction data"),

    // 系统错误
    INTERNAL_SERVER_ERROR("SYSTEM_001", "Internal server error"),
    SERVICE_UNAVAILABLE("SYSTEM_002", "Service temporarily unavailable");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
