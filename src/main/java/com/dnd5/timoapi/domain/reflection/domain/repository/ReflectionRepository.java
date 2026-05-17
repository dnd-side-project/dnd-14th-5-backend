package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ReflectionRepository extends JpaRepository<ReflectionEntity, Long> {

    Optional<ReflectionEntity> findByDateAndUserId(LocalDate date, Long userId);

    List<ReflectionEntity> findAllByDate(LocalDate date);

    @Query("""
            SELECT r
            FROM ReflectionEntity r
            WHERE r.date = :date
              AND r.userId IN (
                    SELECT u.id
                    FROM UserEntity u
                    WHERE u.deletedAt IS NULL
              )
            """)
    List<ReflectionEntity> findAllByDateAndUserDeletedAtIsNull(@Param("date") LocalDate date);

    List<ReflectionEntity> findAllByUserIdAndDateBetween(Long userId, LocalDate start,
            LocalDate end);

    @Query("""
            SELECT r
            FROM ReflectionEntity r
            JOIN ReflectionQuestionEntity q ON r.questionId = q.id
            WHERE r.date = :date
              AND q.category = :category
              AND r.deletedAt IS NULL
              AND q.deletedAt IS NULL
              AND r.userId IN (SELECT u.id FROM UserEntity u WHERE u.deletedAt IS NULL)
            """)
    List<ReflectionEntity> findAllByDateAndQuestionCategory(
            @Param("date") LocalDate date,
            @Param("category") ZtpiCategory category);

    @Query("""
            SELECT r
            FROM ReflectionEntity r
            WHERE r.date = :date
              AND r.userId IN :userIds
              AND r.deletedAt IS NULL
            """)
    List<ReflectionEntity> findAllByDateAndUserIdIn(
            @Param("date") LocalDate date,
            @Param("userIds") List<Long> userIds);
}
