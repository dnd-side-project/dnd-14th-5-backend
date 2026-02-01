package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestRecordEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTestRecordRepository extends JpaRepository<UserTestRecordEntity, Long> {

    List<UserTestRecordEntity> findAllByTestIdOrderBySequenceAsc(Long testId);

}
