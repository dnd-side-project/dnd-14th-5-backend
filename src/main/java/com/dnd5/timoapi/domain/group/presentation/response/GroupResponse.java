package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;

import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String name,
        GroupType type,
        String image,
        ZtpiCategory category,
        int memberCount,
        GroupMemberRole myRole,
        LocalDateTime createdAt
) {
    public static GroupResponse of(Group group, int memberCount, GroupMemberRole myRole) {
        return new GroupResponse(
                group.id(),
                group.name(),
                group.type(),
                group.image(),
                group.category(),
                memberCount,
                myRole,
                group.createdAt()
        );
    }
}
