package com.dnd5.timoapi.domain.customization.domain.repository;

import com.dnd5.timoapi.domain.customization.domain.entity.CustomizationItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomizationItemRepository extends JpaRepository<CustomizationItemEntity, Long> {

    Optional<CustomizationItemEntity> findByIdAndDeletedAtIsNull(Long id);

    List<CustomizationItemEntity> findAllByDeletedAtIsNull();
}
