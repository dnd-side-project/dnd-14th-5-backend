package com.dnd5.timoapi.global.exception;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final String name;
    private final String message;
    private Map<String, Object> additional;

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            ((Enum<?>) errorCode).name(),
            errorCode.getMessage()
        );
    }

    public ErrorResponse addAdditional(Map<String, Object> additional) {
        this.additional = additional;
        return this;
    }
}
