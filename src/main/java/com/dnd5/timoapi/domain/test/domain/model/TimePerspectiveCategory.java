package com.dnd5.timoapi.domain.test.domain.model;

import java.time.LocalDateTime;

public record TimePerspectiveCategory(
        Long id,
        String name,
        String englishName,
        String characterName,
        String personality,
        String description,
        Double idealValue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TimePerspectiveCategory create(String name, String englishName, String characterName, String personality, String description, Double idealValue) {
        return new TimePerspectiveCategory(null, name, englishName, characterName, personality, description, idealValue, null, null);
    }
}
