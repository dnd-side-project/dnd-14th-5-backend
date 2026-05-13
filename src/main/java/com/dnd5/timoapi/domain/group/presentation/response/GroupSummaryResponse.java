package com.dnd5.timoapi.domain.group.presentation.response;

import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;

import java.time.LocalDateTime;

public record GroupSummaryResponse(
        Long id,
        String name,
        GroupType type,
        String image,
        int memberCount,
        GroupMemberRole myRole,
        LocalDateTime createdAt
) {
    public static GroupSummaryResponse of(Group group, int memberCount, GroupMemberRole myRole) {
        return new GroupSummaryResponse(
                group.id(),
                group.name(),
                group.type(),
                group.image(),
                memberCount,
                myRole,
                group.createdAt()
        );
    }
}
