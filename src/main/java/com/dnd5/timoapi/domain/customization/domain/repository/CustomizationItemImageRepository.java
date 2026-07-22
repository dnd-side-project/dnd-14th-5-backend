package com.dnd5.timoapi.domain.customization.domain.repository;

import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationItemImageEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomizationItemImageRepository extends JpaRepository<CustomizationItemImageEntity, Long> {

    List<CustomizationItemImageEntity> findAllByCustomizationItemIdAndDeletedAtIsNull(Long customizationItemId);

    Optional<CustomizationItemImageEntity> findByCustomizationItemIdAndCategoryAndDeletedAtIsNull(
            Long customizationItemId, ZtpiCategory category);

    List<CustomizationItemImageEntity> findAllByCustomizationItemIdInAndCategoryAndDeletedAtIsNull(
            Collection<Long> customizationItemIds, ZtpiCategory category);
}
