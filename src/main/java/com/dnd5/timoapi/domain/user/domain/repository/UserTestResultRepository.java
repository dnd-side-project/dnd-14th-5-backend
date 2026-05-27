package com.dnd5.timoapi.domain.user.domain.repository;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.entity.UserTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTestResultRepository extends JpaRepository<UserTestResultEntity, Long> {

    List<UserTestResultEntity> findByUserTestRecordId(Long userTestRecordId);
    List<UserTestResultEntity> findAllByUserTestRecordId(Long testRecordId);
    List<UserTestResultEntity> findAllByUserTestRecordIdAndDeletedAtIsNull(Long testRecordId);

    Optional<UserTestResultEntity> findFirstByUserTestRecord_User_IdAndCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, ZtpiCategory category);
}
