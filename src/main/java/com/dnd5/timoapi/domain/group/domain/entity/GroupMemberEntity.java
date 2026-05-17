package com.dnd5.timoapi.domain.group.domain.entity;

import com.dnd5.timoapi.domain.group.domain.model.GroupMember;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "group_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"})
)
public class GroupMemberEntity extends BaseEntity {

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupMemberRole role;

    public static GroupMemberEntity from(GroupMember model) {
        return new GroupMemberEntity(model.groupId(), model.userId(), model.role());
    }

    public GroupMember toModel() {
        return new GroupMember(getId(), groupId, userId, role, getCreatedAt(), getUpdatedAt());
    }

    public void promoteToOwner() {
        this.role = GroupMemberRole.OWNER;
    }
}
