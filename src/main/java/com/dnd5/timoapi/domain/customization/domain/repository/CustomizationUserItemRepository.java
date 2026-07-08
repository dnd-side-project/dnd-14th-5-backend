package com.dnd5.timoapi.domain.customization.domain.repository;

import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationUserItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomizationUserItemRepository extends JpaRepository<CustomizationUserItemEntity, Long> {

    List<CustomizationUserItemEntity> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Optional<CustomizationUserItemEntity> findByUserIdAndCustomizationItemIdAndDeletedAtIsNull(
            Long userId, Long customizationItemId);

    List<CustomizationUserItemEntity> findAllByUserIdAndIsEquippedTrueAndDeletedAtIsNull(Long userId);

    List<CustomizationUserItemEntity> findAllByUserIdInAndCustomizationItemIdInAndIsUnlockedTrueAndDeletedAtIsNull(
            Collection<Long> userIds, Collection<Long> customizationItemIds);
}
