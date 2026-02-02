package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionFeedbackPromptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionFeedbackPromptRepository extends
        JpaRepository<ReflectionFeedbackPromptEntity, Long> {
}
