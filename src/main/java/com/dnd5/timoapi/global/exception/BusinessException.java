package com.dnd5.timoapi.global.exception;

import java.util.Map;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> additional; // 선택적 추가 정보

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.additional = null;
    }

    public BusinessException(ErrorCode errorCode, Map<String, Object> additional) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.additional = additional;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getAdditional() {
        return additional;
    }
}
