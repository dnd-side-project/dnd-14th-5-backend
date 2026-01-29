package com.dnd5.timoapi.domain.reflection.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReflectionErrorCode implements ErrorCode {

    REFLECTION_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "회고 질문을 찾을 수 없습니다."),
    REFLECTION_QUESTION_SEQUENCE_DUPLICATED(HttpStatus.CONFLICT, "같은 질문 순서가 존재합니다."),
    USER_REFLECTION_QUESTION_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저의 질문 순서를 찾을 수 없습니다."),
    TODAY_REFLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "오늘의 회고를 찾을 수 없습니다."),
    TODAY_REFLECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "오늘의 회고가 이미 존재합니다."),
    REFLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "회고를 찾을 수 없습니다."),
    REFLECTION_FEEDBACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "회고에 대한 피드백이 이미 존재합니다."),
    REFLECTION_FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "회고 피드백을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
