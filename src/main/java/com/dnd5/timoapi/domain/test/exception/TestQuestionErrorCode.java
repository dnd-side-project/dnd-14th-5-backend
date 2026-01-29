package com.dnd5.timoapi.domain.test.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TestQuestionErrorCode implements ErrorCode {

    TEST_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "테스트 문항을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
