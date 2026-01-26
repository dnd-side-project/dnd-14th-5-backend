package com.dnd5.timoapi.global.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorResponse {

    private final String code;
    private final String message;

    /**
     * Create an ErrorResponse from the given ErrorCode.
     *
     * @param errorCode the ErrorCode to extract the code and message from
     * @return an ErrorResponse containing the code and message from the provided ErrorCode
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage()
        );
    }
}