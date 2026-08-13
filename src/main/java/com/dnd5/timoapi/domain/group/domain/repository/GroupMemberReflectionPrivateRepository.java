package com.dnd5.timoapi.domain.group.domain.repository;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionPrivateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberReflectionPrivateRepository extends JpaRepository<GroupMemberReflectionPrivateEntity, Long> {

    boolean existsByGroupIdAndReflectionId(Long groupId, Long reflectionId);

    List<GroupMemberReflectionPrivateEntity> findAllByGroupIdAndReflectionIdIn(Long groupId, List<Long> reflectionIds);
}
