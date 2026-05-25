package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.enums.FeedbackStatus;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReflectionFeedbackRepository extends
        JpaRepository<ReflectionFeedbackEntity, Long> {

    boolean existsByReflectionId(Long reflectionId);

    Optional<ReflectionFeedbackEntity> findByReflectionId(Long reflectionId);

    @Query("""
            SELECT rf.score FROM ReflectionFeedbackEntity rf
            WHERE rf.reflectionId IN (SELECT r.id FROM ReflectionEntity r WHERE r.userId = :userId)
            AND rf.category = :category
            AND rf.status = :status
            AND rf.createdAt > :afterDate
            AND rf.reflectionId != :excludeReflectionId
            AND rf.deletedAt IS NULL
            """)
    List<Integer> findScoresByUserAndCategoryAfter(
            @Param("userId") Long userId,
            @Param("category") ZtpiCategory category,
            @Param("status") FeedbackStatus status,
            @Param("afterDate") LocalDateTime afterDate,
            @Param("excludeReflectionId") Long excludeReflectionId
    );
}
