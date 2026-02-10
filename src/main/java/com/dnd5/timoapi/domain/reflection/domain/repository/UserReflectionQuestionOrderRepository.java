package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.UserReflectionQuestionOrderEntity;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReflectionQuestionOrderRepository extends
        JpaRepository<UserReflectionQuestionOrderEntity, Long> {

    Optional<UserReflectionQuestionOrderEntity> findByUserIdAndCategory(Long userId,
            ZtpiCategory category);

    boolean existsByUserIdAndCategory(Long userId, ZtpiCategory category);
}
