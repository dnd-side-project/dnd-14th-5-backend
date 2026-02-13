package com.dnd5.timoapi.domain.test.domain.model;

import java.time.LocalDateTime;

public record TimePerspectiveCategory(
        Long id,
        String name,
        String character,
        String personality,
        String description,
        LocalDateTime createdAt
) {
    public static TimePerspectiveCategory create(String name, String character, String personality, String description) {
        return new TimePerspectiveCategory(null, name, character, personality, description, null);
    }
}
