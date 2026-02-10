package com.dnd5.timoapi.domain.user.domain.repository;

import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByIdAndDeletedAtIsNull(Long id);
    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    List<UserEntity> findAllByStreakDaysGreaterThanAndDeletedAtIsNull(Integer streakDays);
}
