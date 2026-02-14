package com.dnd5.timoapi.domain.introduction.domain.repository;

import com.dnd5.timoapi.domain.introduction.domain.entity.IntroductionEntity;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntroductionRepository extends JpaRepository<IntroductionEntity, Long> {
    List<IntroductionEntity> findAllByDeletedAtIsNull();
    Optional<IntroductionEntity> findByVersionAndDeletedAtIsNull(int version);

    List<IntroductionEntity> findAllByVersionAndDeletedAtIsNull(int version);
}
