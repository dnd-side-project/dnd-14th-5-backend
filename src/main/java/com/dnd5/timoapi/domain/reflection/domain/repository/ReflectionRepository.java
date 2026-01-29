package com.dnd5.timoapi.domain.reflection.domain.repository;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionRepository extends JpaRepository<ReflectionEntity, Long> {

    Optional<ReflectionEntity> findByDateAndUserId(LocalDate date, Long userId);

    Page<ReflectionEntity> findAllByUserId(Long userId, Pageable pageable);

    List<ReflectionEntity> findAllByDate(LocalDate date);
}
