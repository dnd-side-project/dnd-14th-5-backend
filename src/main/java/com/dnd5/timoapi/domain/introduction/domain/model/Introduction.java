package com.dnd5.timoapi.domain.introduction.domain.model;

import java.time.LocalDateTime;

public record Introduction(
        Long id,
        int version,
        Long sequence,
        String title,
        String description,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static Introduction create(int version, Long sequence, String title, String description, String imageUrl) {
        return new Introduction(null, version, sequence, title, description, imageUrl, null);
    }
}
