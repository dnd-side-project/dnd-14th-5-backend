package com.dnd5.timoapi.domain.group.domain.model;

import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;

import java.time.LocalDateTime;

public record Group(
        Long id,
        String code,
        String name,
        GroupType type,
        String image,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Group create(String code, String name, GroupType type, String image) {
        return new Group(null, code, name, type, image, null, null);
    }
}
