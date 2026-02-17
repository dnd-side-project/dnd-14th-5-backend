package com.dnd5.timoapi.domain.test.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TimePerspectiveCategoryErrorCode implements ErrorCode {

    TIME_PERSPECTIVE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "시간관 카테고리를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
