package com.dnd5.timoapi.domain.fcm.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FcmErrorCode implements ErrorCode {

    DEVICE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "기기 토큰을 찾을 수 없습니다."),
    DEVICE_TOKEN_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 기기 토큰입니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
