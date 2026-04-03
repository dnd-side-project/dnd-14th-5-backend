package com.dnd5.timoapi.domain.user.domain.repository;

import com.dnd5.timoapi.domain.user.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserTestRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTestRecordRepository extends JpaRepository<UserTestRecordEntity, Long> {

    List<UserTestRecordEntity> findByUserId(Long userId);

    Optional<UserTestRecordEntity> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, UserTestRecordStatus status);

    Optional<UserTestRecordEntity> findByUserIdAndTestIdAndStatus(Long userId, Long testId, UserTestRecordStatus status);
    Optional<UserTestRecordEntity> findByUserIdAndTestIdAndStatusAndDeletedAtIsNull(Long userId, Long id, UserTestRecordStatus testRecordStatus);
    Optional<UserTestRecordEntity> findByIdAndDeletedAtIsNull(Long testRecordId);
    List<UserTestRecordEntity> findByUserIdAndDeletedAtIsNull(Long userId);
    List<UserTestRecordEntity> findByTestIdAndDeletedAtIsNull(Long testId);
}
