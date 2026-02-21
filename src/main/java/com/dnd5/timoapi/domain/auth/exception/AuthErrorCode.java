package com.dnd5.timoapi.domain.auth.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),
    ILLEGAL_REGISTRATION_ID(HttpStatus.BAD_REQUEST, "사용할 수 없는 registration 아이디입니다."),
    MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "refresh_token 쿠키가 없습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
