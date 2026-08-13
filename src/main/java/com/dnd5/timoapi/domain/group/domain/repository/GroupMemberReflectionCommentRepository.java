package com.dnd5.timoapi.domain.group.domain.repository;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionCommentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberReflectionCommentRepository extends JpaRepository<GroupMemberReflectionCommentEntity, Long> {

    long countByGroupIdAndReflectionIdAndDeletedAtIsNull(Long groupId, Long reflectionId);

    List<GroupMemberReflectionCommentEntity> findAllByGroupIdAndReflectionIdAndDeletedAtIsNullOrderByCreatedAtAsc(
            Long groupId, Long reflectionId);

    Optional<GroupMemberReflectionCommentEntity> findByIdAndGroupIdAndReflectionIdAndDeletedAtIsNull(
            Long id, Long groupId, Long reflectionId);
}
