package com.dnd5.timoapi.domain.group.domain.repository;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionLikeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberReflectionLikeRepository extends JpaRepository<GroupMemberReflectionLikeEntity, Long> {

    long countByGroupIdAndReflectionId(Long groupId, Long reflectionId);

    boolean existsByGroupIdAndReflectionIdAndUserId(Long groupId, Long reflectionId, Long userId);

    Optional<GroupMemberReflectionLikeEntity> findByGroupIdAndReflectionIdAndUserId(Long groupId, Long reflectionId, Long userId);
}
