package com.dnd5.timoapi.domain.notification.application.service;

import com.dnd5.timoapi.domain.notification.domain.entity.AlarmSettingEntity;
import com.dnd5.timoapi.domain.notification.domain.repository.AlarmSettingRepository;
import com.dnd5.timoapi.domain.notification.exception.NotificationErrorCode;
import com.dnd5.timoapi.domain.notification.presentation.response.ScheduleResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.infrastructure.fcm.FcmMessage;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationScheduleService {

    private final AlarmSettingRepository alarmSettingRepository;
    private final FcmService fcmService;

    public void create(LocalTime notificationTime, String token) {
        Long userId = SecurityUtil.getCurrentUserId();

        if (alarmSettingRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            throw new BusinessException(NotificationErrorCode.SCHEDULE_ALREADY_EXISTS);
        }

        alarmSettingRepository.save(new AlarmSettingEntity(userId, notificationTime));
        fcmService.registerDeviceToken(token);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getMy() {
        Long userId = SecurityUtil.getCurrentUserId();

        AlarmSettingEntity entity = alarmSettingRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.SCHEDULE_NOT_FOUND));

        return ScheduleResponse.from(entity);
    }

    public void update(Long scheduleId, LocalTime notificationTime) {
        AlarmSettingEntity entity = findByIdAndValidateOwner(scheduleId);
        entity.updateAlarmTime(notificationTime);
    }

    public void delete(Long scheduleId) {
        AlarmSettingEntity entity = findByIdAndValidateOwner(scheduleId);
        entity.setDeletedAt(LocalDateTime.now());
    }

    public void testSend() {
        Long userId = SecurityUtil.getCurrentUserId();
        fcmService.sendToUser(userId, FcmMessage.of("테스트", "알림 테스트 성공"));
    }

    private AlarmSettingEntity findByIdAndValidateOwner(Long scheduleId) {
        Long userId = SecurityUtil.getCurrentUserId();

        AlarmSettingEntity entity = alarmSettingRepository.findById(scheduleId)
                .filter(e -> e.getDeletedAt() == null)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.SCHEDULE_NOT_FOUND));

        return entity;
    }
}
