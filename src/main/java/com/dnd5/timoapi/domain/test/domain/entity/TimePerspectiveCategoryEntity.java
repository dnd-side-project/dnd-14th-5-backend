package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "time_perspective_categories")
public class TimePerspectiveCategoryEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String character;

    @Column(nullable = false)
    private String personality;

    @Column(columnDefinition = "TEXT")
    private String description;

    public static TimePerspectiveCategoryEntity from(TimePerspectiveCategory model) {
        return new TimePerspectiveCategoryEntity(model.name(), model.character(), model.personality(), model.description());
    }

    public TimePerspectiveCategory toModel() {
        return new TimePerspectiveCategory(getId(), getName(), getCharacter(), getPersonality(), getDescription(), getCreatedAt());
    }

}
