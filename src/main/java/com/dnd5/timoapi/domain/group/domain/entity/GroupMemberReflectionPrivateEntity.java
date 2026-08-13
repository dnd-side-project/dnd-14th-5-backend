package com.dnd5.timoapi.domain.group.domain.entity;

import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "user_group_member_reflection_privates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "reflection_id"})
)
public class GroupMemberReflectionPrivateEntity extends BaseEntity {

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "reflection_id", nullable = false)
    private Long reflectionId;
}
