package com.dnd5.timoapi.domain.test.domain.repository;

import com.dnd5.timoapi.domain.test.domain.entity.UserTestResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTestResultRepository extends JpaRepository<UserTestResultEntity, Long> {
}
