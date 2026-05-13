package com.dnd5.timoapi.domain.group.domain.model;

import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;

import java.time.LocalDateTime;

public record GroupMember(
        Long id,
        Long groupId,
        Long userId,
        GroupMemberRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GroupMember create(Long groupId, Long userId, GroupMemberRole role) {
        return new GroupMember(null, groupId, userId, role, null, null);
    }
}
