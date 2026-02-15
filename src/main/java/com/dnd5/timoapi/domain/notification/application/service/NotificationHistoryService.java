package com.dnd5.timoapi.domain.notification.application.service;

import com.dnd5.timoapi.domain.notification.domain.entity.NotificationHistoryEntity;
import com.dnd5.timoapi.domain.notification.domain.repository.NotificationHistoryRepository;
import com.dnd5.timoapi.domain.notification.exception.NotificationErrorCode;
import com.dnd5.timoapi.domain.notification.presentation.response.NotificationHistoryResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationHistoryService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    @Transactional(readOnly = true)
    public List<NotificationHistoryResponse> getMyNotificationHistory() {
        Long userId = SecurityUtil.getCurrentUserId();

        List<NotificationHistoryEntity> notificationHistoryEntities =
                notificationHistoryRepository.findAllByUserIdAndIsReadFalse(userId);

        return notificationHistoryEntities.stream()
                .map(NotificationHistoryEntity::toModel)
                .map(NotificationHistoryResponse::from)
                .toList();
    }

    public void updateNotificationHistory(Long historyId) {
        NotificationHistoryEntity notificationHistoryEntity =
                notificationHistoryRepository.findById(historyId)
                        .orElseThrow(() -> new BusinessException(
                                NotificationErrorCode.NOTIFICATION_HISTORY_NOT_FOUND
                        ));

        notificationHistoryEntity.read();
    }
}
