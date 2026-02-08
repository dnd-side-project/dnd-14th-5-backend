package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTestResultRepository extends JpaRepository<UserTestResultEntity, Long> {

    List<UserTestResultEntity> findByUserTestRecordId(Long userTestRecordId);

}
