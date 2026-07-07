package com.dnd5.timoapi.domain.customization.domain.entity;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationUserItem;
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
        name = "customization_user_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "customization_item_id"})
)
public class CustomizationUserItemEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "customization_item_id", nullable = false)
    private Long customizationItemId;

    @Column(nullable = false)
    private boolean isUnlocked;

    @Column(nullable = false)
    private boolean isEquipped;

    public static CustomizationUserItemEntity from(CustomizationUserItem model) {
        return new CustomizationUserItemEntity(
                model.userId(),
                model.customizationItemId(),
                model.isUnlocked(),
                model.isEquipped()
        );
    }

    public CustomizationUserItem toModel() {
        return new CustomizationUserItem(
                getId(),
                getUserId(),
                getCustomizationItemId(),
                isUnlocked(),
                isEquipped(),
                getCreatedAt(),
                getUpdatedAt(),
                getDeletedAt()
        );
    }

    public void lock() { this.isUnlocked = false; }

    public void unlock() { this.isUnlocked = true; }

    public void equip() {
        this.isEquipped = true;
    }

    public void unequip() {
        this.isEquipped = false;
    }
}
