package com.dnd5.timoapi.domain.statistics.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StatisticsErrorCode implements ErrorCode {

    STATISTICS_TEST_NOT_COMPLETED(HttpStatus.NOT_FOUND, "완료된 ZTPI 테스트 기록이 없습니다."),
    STATISTICS_TEST_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "테스트 결과 데이터가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
