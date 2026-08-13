package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;

public record GroupReflectionCommentResponse(
        Long id,
        Long commenterId,
        String commenterNickname,
        ZtpiCategory commenterCategory,
        String content,
        LocalDateTime createdAt
) {
}
