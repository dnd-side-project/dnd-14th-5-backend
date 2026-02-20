package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
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
}
