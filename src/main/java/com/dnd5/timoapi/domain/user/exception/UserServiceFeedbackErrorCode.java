package com.dnd5.timoapi.domain.user.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserServiceFeedbackErrorCode implements ErrorCode {

    USER_SERVICE_FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "유저의 서비스 피드백을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
