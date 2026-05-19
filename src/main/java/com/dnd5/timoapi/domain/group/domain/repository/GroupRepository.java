package com.dnd5.timoapi.domain.group.domain.repository;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    Optional<GroupEntity> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByTypeAndCategoryAndDeletedAtIsNull(GroupType type, ZtpiCategory category);
}
