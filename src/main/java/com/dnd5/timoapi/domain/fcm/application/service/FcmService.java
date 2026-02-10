package com.dnd5.timoapi.domain.fcm.application.service;

import com.dnd5.timoapi.domain.fcm.domain.entity.FcmTokenEntity;
import com.dnd5.timoapi.domain.fcm.domain.repository.FcmTokenRepository;
import com.dnd5.timoapi.domain.fcm.exception.FcmErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.infrastructure.fcm.FcmMessage;
import com.dnd5.timoapi.global.infrastructure.fcm.FcmSender;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FcmSender fcmSender;

    public void registerDeviceToken(String token) {
        Long userId = SecurityUtil.getCurrentUserId();

        if (fcmTokenRepository.existsByUserIdAndTokenAndDeletedAtIsNull(userId, token)) {
            return;
        }

        fcmTokenRepository.save(new FcmTokenEntity(userId, token));
    }

    public void deleteDeviceToken(String token) {
        Long userId = SecurityUtil.getCurrentUserId();

        FcmTokenEntity entity = fcmTokenRepository
                .findByUserIdAndTokenAndDeletedAtIsNull(userId, token)
                .orElseThrow(() -> new BusinessException(FcmErrorCode.DEVICE_TOKEN_NOT_FOUND));

        entity.setDeletedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public void sendToUser(Long userId, FcmMessage message) {
        List<String> tokens = fcmTokenRepository.findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(FcmTokenEntity::getToken)
                .toList();

        if (tokens.isEmpty()) return;

        fcmSender.sendToTokens(tokens, message);
    }
}
