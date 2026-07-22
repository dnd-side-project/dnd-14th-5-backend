package com.dnd5.timoapi.domain.group.domain.repository;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    Optional<GroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<GroupEntity> findByCodeAndDeletedAtIsNull(String code);

    Optional<GroupEntity> findByTypeAndCategoryAndDeletedAtIsNull(GroupType type, ZtpiCategory category);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByTypeAndCategoryAndDeletedAtIsNull(GroupType type, ZtpiCategory category);

    List<GroupEntity> findAllByTypeAndDeletedAtIsNull(GroupType type);

    List<GroupEntity> findAllDeletedAtIsNull();
}
