package com.dnd5.timoapi.domain.group.domain.entity;

import com.dnd5.timoapi.domain.group.domain.model.Group;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "user_groups")
public class GroupEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupType type;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column
    @Enumerated(EnumType.STRING)
    private ZtpiCategory category;

    public static GroupEntity from(Group model) {
        return new GroupEntity(model.code(), model.name(), model.type(), model.image(), model.category());
    }

    public Group toModel() {
        return new Group(getId(), code, name, type, image, category, getCreatedAt(), getUpdatedAt());
    }

    public void update(String name, String image) {
        if (name != null) this.name = name;
        if (image != null) this.image = image;
    }
}
