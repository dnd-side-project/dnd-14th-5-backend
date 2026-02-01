package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTestRecordRepository extends JpaRepository<UserTestRecordEntity, Long> {

    List<UserTestRecordEntity> findByUserId(Long userId);

}
