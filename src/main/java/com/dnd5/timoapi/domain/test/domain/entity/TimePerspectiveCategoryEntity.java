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

    @Column(name = "english_name", nullable = false)
    private String englishName;

    @Column(name = "character_name", nullable = false)
    private String characterName;

    @Column(nullable = false)
    private String personality;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ideal_value", nullable = false)
    private Double idealValue;

    public static TimePerspectiveCategoryEntity from(TimePerspectiveCategory model) {
        return new TimePerspectiveCategoryEntity(model.name(), model.englishName(), model.characterName(), model.personality(), model.description(), model.idealValue());
    }

    public TimePerspectiveCategory toModel() {
        return new TimePerspectiveCategory(getId(), getName(), getEnglishName(), getCharacterName(), getPersonality(), getDescription(), getIdealValue(), getCreatedAt(), getUpdatedAt());
    }

    public void update(String name, String englishName, String characterName, String personality, String description, Double idealValue) {
        if (name != null) this.name = name;
        if (englishName != null) this.englishName = englishName;
        if (characterName != null) this.characterName = characterName;
        if (personality != null) this.personality = personality;
        if (description != null) this.description = description;
        if (idealValue != null) this.idealValue = idealValue;
    }

}
