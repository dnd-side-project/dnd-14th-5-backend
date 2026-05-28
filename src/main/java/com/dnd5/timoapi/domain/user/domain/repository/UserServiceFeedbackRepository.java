package com.dnd5.timoapi.domain.user.domain.repository;

import com.dnd5.timoapi.domain.user.domain.entity.UserServiceFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserServiceFeedbackRepository extends JpaRepository<UserServiceFeedbackEntity, Long> {
    Optional<UserServiceFeedbackEntity> findByIdAndDeletedAtIsNull(Long id);
    List<UserServiceFeedbackEntity> findByDeletedAtIsNull();

    boolean existsByUserIdAndDeletedAtIsNull(Long userId);
}
