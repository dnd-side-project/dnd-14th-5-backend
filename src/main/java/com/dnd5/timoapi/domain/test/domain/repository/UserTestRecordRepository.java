package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTestRecordRepository extends JpaRepository<UserTestRecordEntity, Long> {

    List<UserTestRecordEntity> findByUserId(Long userId);

    Optional<UserTestRecordEntity> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, TestRecordStatus status);

}
