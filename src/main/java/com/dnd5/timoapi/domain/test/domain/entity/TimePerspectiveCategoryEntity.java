package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.TimePerspectiveCategory;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
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

    @Column(name = "character_name", nullable = false)
    private String characterName;

    @Column(nullable = false)
    private String personality;

    @Column(columnDefinition = "TEXT")
    private String description;

    public static TimePerspectiveCategoryEntity from(TimePerspectiveCategory model) {
        return new TimePerspectiveCategoryEntity(model.name(), model.characterName(), model.personality(), model.description());
    }

    public TimePerspectiveCategory toModel() {
        return new TimePerspectiveCategory(getId(), getName(), getCharacterName(), getPersonality(), getDescription(), getCreatedAt(), getUpdatedAt());
    }

    public void update(String name, String characterName, String personality, String description) {
        if (name != null) this.name = name;
        if (characterName != null) this.characterName = characterName;
        if (personality != null) this.personality = personality;
        if (description != null) this.description = description;
    }

}
