package com.dnd5.timoapi.domain.test.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TestErrorCode implements ErrorCode {

    TEST_NOT_FOUND(HttpStatus.NOT_FOUND, "테스트를 찾을 수 없습니다."),
    TEST_TYPE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 테스트 타입입니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
