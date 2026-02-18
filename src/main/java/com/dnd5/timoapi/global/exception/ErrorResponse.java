package com.dnd5.timoapi.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final String name;
    private final String message;

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            ((Enum<?>) errorCode).name(),
            errorCode.getMessage()
        );
    }
}
