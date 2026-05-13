package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;

import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String code,
        String name,
        GroupType type,
        String image,
        int memberCount,
        Boolean isMember,
        GroupMemberRole myRole,
        LocalDateTime createdAt
) {
    public static GroupResponse of(Group group, int memberCount, Boolean isMember, GroupMemberRole myRole) {
        return new GroupResponse(
                group.id(),
                group.code(),
                group.name(),
                group.type(),
                group.image(),
                memberCount,
                isMember,
                myRole,
                group.createdAt()
        );
    }
}
