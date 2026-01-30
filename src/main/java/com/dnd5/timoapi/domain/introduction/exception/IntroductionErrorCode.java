package com.dnd5.timoapi.domain.introduction.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IntroductionErrorCode implements ErrorCode {

    INTRODUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "서비스 소개를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
