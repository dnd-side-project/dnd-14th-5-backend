package com.dnd5.timoapi.domain.introduction.domain.model;

import java.time.LocalDateTime;

public record Introduction(
        Long id,
        int version,
        String content,
        LocalDateTime createdAt
) {
    public static Introduction create(int version, String content) {
        return new Introduction(null, version, content, null);
    }
}
