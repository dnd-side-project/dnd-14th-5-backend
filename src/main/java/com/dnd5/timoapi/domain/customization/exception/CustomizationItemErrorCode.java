package com.dnd5.timoapi.domain.customization.exception;

import com.dnd5.timoapi.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomizationItemErrorCode implements ErrorCode {

    CUSTOMIZATION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "커스터마이징 아이템을 찾을 수 없습니다."),
    CUSTOMIZATION_ITEM_NOT_UNLOCKED(HttpStatus.FORBIDDEN, "해금되지 않은 커스터마이징 아이템입니다. (customizationItemId: %s)"),
    CUSTOMIZATION_THEME_ALREADY_EQUIPPED(HttpStatus.CONFLICT, "이미 장착 중인 테마가 있습니다. 먼저 해제해주세요. (equippedItemId: %s)"),
    ;

    private final HttpStatus status;
    private final String message;
}
