package com.dnd5.timoapi.domain.group.domain.repository;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    Optional<GroupMemberEntity> findByGroupIdAndUserIdAndDeletedAtIsNull(Long groupId, Long userId);

    Optional<GroupMemberEntity> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMemberEntity> findAllByGroupIdAndDeletedAtIsNull(Long groupId);

    List<GroupMemberEntity> findAllByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByGroupIdAndUserIdAndDeletedAtIsNull(Long groupId, Long userId);

    long countByGroupIdAndDeletedAtIsNull(Long groupId);

    Optional<GroupMemberEntity> findTopByGroupIdAndRoleAndDeletedAtIsNullOrderByCreatedAtAsc(Long groupId, GroupMemberRole role);
}
