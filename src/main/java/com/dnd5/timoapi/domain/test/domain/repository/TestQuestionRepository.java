package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestionEntity, Long> {

    List<TestQuestionEntity> findByTestIdOrderBySequenceAsc(Long testId);
    Optional<TestQuestionEntity> findByIdAndTestIdAndDeletedAtIsNull(Long id, Long testId);

}
