package com.dnd5.timoapi.domain.group.domain.model;

import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

import java.time.LocalDateTime;

public record Group(
        Long id,
        String code,
        String name,
        GroupType type,
        String image,
        ZtpiCategory category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Group create(String code, String name, GroupType type, String image, ZtpiCategory category) {
        return new Group(null, code, name, type, image, category, null, null);
    }
}
