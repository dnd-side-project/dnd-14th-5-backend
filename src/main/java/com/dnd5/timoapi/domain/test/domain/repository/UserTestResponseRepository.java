package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestResponseEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTestResponseRepository extends JpaRepository<UserTestResponseEntity, Long> {

    int countByUserTestRecordId(Long testRecordId);
    Optional<List<UserTestResponseEntity>> findByUserTestRecordId(Long testRecordId);
    Optional<UserTestResponseEntity> findByUserTestRecordIdAndId(Long testRecordId, Long id);
}
