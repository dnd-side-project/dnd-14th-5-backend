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
    GROUP_REFLECTION_LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 좋아요를 누른 회고입니다."),
    GROUP_REFLECTION_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요를 찾을 수 없습니다."),
    GROUP_REFLECTION_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    GROUP_REFLECTION_COMMENT_NOT_OWNER(HttpStatus.FORBIDDEN, "댓글 작성자만 수정하거나 삭제할 수 있습니다. (commentId: %s, commentUserId: %s, currentUserId: %s)"),
    ;

    private final HttpStatus status;
    private final String message;
}
