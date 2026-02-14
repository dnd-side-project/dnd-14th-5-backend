package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.TestEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    List<TestEntity> findAllByDeletedAtIsNull();
    Optional<TestEntity> findByIdAndDeletedAtIsNull(Long id);
    Optional<TestEntity> findByTypeAndDeletedAtIsNull(TestType testType);
    boolean existsByTypeAndDeletedAtIsNull(TestType type);
}
