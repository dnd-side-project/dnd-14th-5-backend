package com.dnd5.timoapi.domain.test.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserTestResponseErrorCode implements ErrorCode {

    USER_TEST_ALREADY_RESPONSE(HttpStatus.CONFLICT, "유저의 테스트 문항이 이미 답변되었습니다."),
    USER_TEST_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "유저의 테스트 문항 답변을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
