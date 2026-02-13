package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.TestQuestionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestQuestionRepository extends JpaRepository<TestQuestionEntity, Long> {

    Optional<TestQuestionEntity> findByIdAndTestIdAndDeletedAtIsNull(Long id, Long testId);
    Optional<List<TestQuestionEntity>> findByTestId(Long id);
    List<TestQuestionEntity> findByTestIdAndDeletedAtIsNullOrderBySequenceAsc(Long testId);
    int countByTestIdAndDeletedAtIsNull(Long testId);
    boolean existsByTestIdAndSequenceAndDeletedAtIsNull(Long testId, int sequence);
    boolean existsByTestIdAndSequenceAndIdNotAndDeletedAtIsNull(Long testId, int sequence, Long id);
}
