package com.dnd5.timoapi.domain.test.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserTestResponseErrorCode implements ErrorCode {

    USER_TEST_ALREADY_RESPONSE(HttpStatus.CONFLICT, "유저의 테스트 문항이 이미 답변되었습니다. (testResponseId: %s)"),
    USER_TEST_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "유저의 테스트 문항 답변을 찾을 수 없습니다."),
    USER_TEST_CROSS_RESPONSE(HttpStatus.BAD_REQUEST, "답변이 등록되는 테스트가 올바르지 않습니다. (userTestId: %s, questionTestId: %s)"),
    USER_TEST_NOT_OWNER(HttpStatus.FORBIDDEN, "테스트 소유권이 없습니다. (recordId: %s, recordUserId: %s, currentUserId: %s)"),
    USER_TEST_ALREADY_COMPLETE(HttpStatus.BAD_REQUEST, "유저의 테스트 기록이 이미 완료되었습니다. (userTestRecordId: %s, status: %s)"),
    USER_TEST_RESPONSE_NOT_BELONG(HttpStatus.BAD_REQUEST, "해당 테스트 기록에 속하지 않는 답변입니다. (testRecordId: %s, responseId: %s, responseTestRecordId: %s)")
    ;

    private final HttpStatus status;
    private final String message;
}
