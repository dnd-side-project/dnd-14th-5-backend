package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestResponseEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTestResponseRepository extends JpaRepository<UserTestResponseEntity, Long> {
    List<UserTestResponseEntity> findByTestRecordId(Long testRecordId);
}
