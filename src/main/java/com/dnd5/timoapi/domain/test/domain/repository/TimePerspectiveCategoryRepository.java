package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.TimePerspectiveCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimePerspectiveCategoryRepository extends JpaRepository<TimePerspectiveCategoryEntity, Long> {

    List<TimePerspectiveCategoryEntity> findAllByEnglishNameAndDeletedAtIsNull(String englishName);
}
