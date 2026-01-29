package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReflectionQuestionRepository extends
        JpaRepository<ReflectionQuestionEntity, Long> {

    @Query("""
            SELECT q FROM ReflectionQuestionEntity q
            WHERE q.deletedAt IS NULL
              AND (:keyword IS NULL OR q.content LIKE %:keyword%)
              AND (:category IS NULL OR q.category = :category)
            """)
    Page<ReflectionQuestionEntity> searchByConditions(
            @Param("keyword") String keyword,
            @Param("category") ZtpiCategory category,
            Pageable pageable
    );

    Optional<ReflectionQuestionEntity> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySequenceAndCategory(Long sequence, ZtpiCategory category);

    boolean existsBySequenceAndCategoryAndIdNot(Long sequence, ZtpiCategory category, Long id);

    Optional<ReflectionQuestionEntity> findBySequenceAndCategory(Long sequence,
            ZtpiCategory category);

    @Query("SELECT COALESCE(MAX(q.sequence), 0) FROM ReflectionQuestionEntity q WHERE q.category = :category AND q.deletedAt IS NULL")
    Long findMaxSequenceByCategory(@Param("category") ZtpiCategory category);

    @Query("SELECT q FROM ReflectionQuestionEntity q WHERE q.category = :category AND q.sequence > :sequence AND q.deletedAt IS NULL")
    java.util.List<ReflectionQuestionEntity> findAllByCategoryAndSequenceGreaterThan(
            @Param("category") ZtpiCategory category,
            @Param("sequence") Long sequence);
}
