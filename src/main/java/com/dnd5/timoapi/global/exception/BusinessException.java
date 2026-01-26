package com.dnd5.timoapi.global.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Creates a BusinessException that wraps the given ErrorCode.
     *
     * @param errorCode the ErrorCode to associate with this exception; its message will be used as the exception message
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Retrieve the ErrorCode associated with this exception.
     *
     * @return the ErrorCode stored in this BusinessException
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}