package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTestRecordRepository extends JpaRepository<UserTestRecordEntity, Long> {

    List<UserTestRecordEntity> findByUserId(Long userId);

    Optional<UserTestRecordEntity> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, TestRecordStatus status);

    Optional<UserTestRecordEntity> findByUserIdAndTestIdAndStatus(Long userId, Long testId, TestRecordStatus status);
    Optional<UserTestRecordEntity> findByUserIdAndTestIdAndStatusAndDeletedAtIsNull(Long userId, Long id, TestRecordStatus testRecordStatus);
    Optional<UserTestRecordEntity> findByIdAndDeletedAtIsNull(Long testRecordId);
    List<UserTestRecordEntity> findByUserIdAndDeletedAtIsNull(Long userId);
}
