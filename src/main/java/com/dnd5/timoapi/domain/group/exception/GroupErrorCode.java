package com.dnd5.timoapi.domain.group.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {

    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없습니다."),
    GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹 멤버를 찾을 수 없습니다."),
    GROUP_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여한 그룹입니다."),
    GROUP_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 관리 권한이 없습니다."),
    GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "그룹에 접근할 권한이 없습니다."),
    GROUP_INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "캐릭터 그룹에는 ZTPI 캐릭터를 지정해야 합니다."),
    GROUP_CATEGORY_NOT_SET(HttpStatus.BAD_REQUEST, "캐릭터 유형이 설정되지 않았습니다."),
    GROUP_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "그룹 타입을 지정해야 합니다."),
    GROUP_CHARACTER_IMMUTABLE(HttpStatus.FORBIDDEN, "캐릭터 그룹은 수정하거나 삭제할 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
