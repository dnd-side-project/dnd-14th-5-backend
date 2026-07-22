package com.dnd5.timoapi.domain.customization.domain.entity;

import com.dnd5.timoapi.domain.customization.domain.model.CustomizationItemImage;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
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
        name = "customization_item_images",
        uniqueConstraints = @UniqueConstraint(columnNames = {"customization_item_id", "category"})
)
public class CustomizationItemImageEntity extends BaseEntity {

    @Column(name = "customization_item_id", nullable = false)
    private Long customizationItemId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ZtpiCategory category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String image;

    @Column(name = "image_without_background", columnDefinition = "TEXT", nullable = false)
    private String imageWithoutBackground;

    public static CustomizationItemImageEntity from(CustomizationItemImage model) {
        return new CustomizationItemImageEntity(
                model.customizationItemId(), model.category(), model.image(), model.imageWithoutBackground());
    }

    public CustomizationItemImage toModel() {
        return new CustomizationItemImage(
                getId(),
                getCustomizationItemId(),
                getCategory(),
                getImage(),
                getImageWithoutBackground(),
                getCreatedAt(),
                getUpdatedAt(),
                getDeletedAt()
        );
    }

    public void updateImage(String image, String imageWithoutBackground) {
        this.image = image;
        this.imageWithoutBackground = imageWithoutBackground;
    }
}
