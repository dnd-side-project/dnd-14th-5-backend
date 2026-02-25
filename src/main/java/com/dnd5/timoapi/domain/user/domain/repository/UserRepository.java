package com.dnd5.timoapi.domain.user.domain.repository;

import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByIdAndDeletedAtIsNull(Long id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    List<UserEntity> findAllByStreakDaysGreaterThanAndDeletedAtIsNull(Integer streakDays);

    @Modifying
    @Query("UPDATE UserEntity u SET u.streakDays = 0 WHERE u.streakDays > 0 AND u.deletedAt IS NULL AND u.id NOT IN :activeUserIds")
    void resetStreakForInactiveUsers(@Param("activeUserIds") Collection<Long> activeUserIds);
}
