package com.dnd5.timoapi.domain.test.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserTestRecordErrorCode implements ErrorCode {

    USER_TEST_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "유저의 테스트 기록을 찾을 수 없습니다."),
    ALREADY_COMPLETED(HttpStatus.CONFLICT, "유저의 테스트 기록이 이미 완료되었습니다."),
    NOT_ALL_QUESTIONS_ANSWERED(HttpStatus.BAD_REQUEST, "유저가 테스트 문항에 모두 응답하지 않았습니다."),
    USER_TEST_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "테스트 기록의 결과가 존재하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
