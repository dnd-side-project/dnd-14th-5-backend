package com.dnd5.timoapi.domain.fcm.domain.repository;

import com.dnd5.timoapi.domain.fcm.domain.entity.FcmTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

    List<FcmTokenEntity> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<FcmTokenEntity> findByUserIdAndTokenAndDeletedAtIsNull(Long userId, String token);

    boolean existsByUserIdAndTokenAndDeletedAtIsNull(Long userId, String token);
}
