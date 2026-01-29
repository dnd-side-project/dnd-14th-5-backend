package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionFeedbackRepository extends
        JpaRepository<ReflectionFeedbackEntity, Long> {

    boolean existsByReflectionId(Long reflectionId);

    Optional<ReflectionFeedbackEntity> findByReflectionId(Long reflectionId);
}
