package com.dnd5.timoapi.domain.customization.domain.entity;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItem;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationItemType;
import com.dnd5.timoapi.domain.customization.domain.model.enums.CustomizationUnlockConditionType;
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
@Table(name = "customization_items")
public class CustomizationItemEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomizationItemType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomizationUnlockConditionType unlockConditionType;

    @Column(nullable = false)
    private Integer unlockConditionCount;

    @Column(columnDefinition = "TEXT")
    private String image;

    public static CustomizationItemEntity from(CustomizationItem model) {
        return new CustomizationItemEntity(
                model.name(),
                model.type(),
                model.description(),
                model.unlockConditionType(),
                model.unlockConditionCount(),
                model.image()
        );
    }

    public CustomizationItem toModel() {
        return new CustomizationItem(
                getId(),
                getName(),
                getType(),
                getDescription(),
                getUnlockConditionType(),
                getUnlockConditionCount(),
                getImage(),
                getCreatedAt(),
                getUpdatedAt(),
                getDeletedAt()
        );
    }

    public void update(
            String name,
            CustomizationItemType type,
            String description,
            CustomizationUnlockConditionType unlockConditionType,
            Integer unlockConditionCount,
            String image
    ) {
        if (name != null) this.name = name;
        if (type != null) this.type = type;
        if (description != null) this.description = description;
        if (unlockConditionType != null) this.unlockConditionType = unlockConditionType;
        if (unlockConditionCount != null) this.unlockConditionCount = unlockConditionCount;
        if (image != null) this.image = image;
    }
}
