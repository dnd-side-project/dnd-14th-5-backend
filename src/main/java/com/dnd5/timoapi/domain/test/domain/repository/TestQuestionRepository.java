package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestionEntity, Long> {

    Optional<TestQuestionEntity> findByIdAndTestIdAndDeletedAtIsNull(Long id, Long testId);
    Optional<List<TestQuestionEntity>> findByTestId(Long id);
    List<TestQuestionEntity> findByTestIdAndDeletedAtIsNullOrderBySequenceAsc(Long testId);
}
