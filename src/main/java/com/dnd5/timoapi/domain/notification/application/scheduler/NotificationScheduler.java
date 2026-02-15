package com.dnd5.timoapi.domain.notification.application.scheduler;

import com.dnd5.timoapi.domain.notification.application.service.FcmService;
import com.dnd5.timoapi.domain.notification.domain.entity.AlarmSettingEntity;
import com.dnd5.timoapi.domain.notification.domain.entity.NotificationHistoryEntity;
import com.dnd5.timoapi.domain.notification.domain.model.NotificationHistory;
import com.dnd5.timoapi.domain.notification.domain.repository.AlarmSettingRepository;
import com.dnd5.timoapi.domain.notification.domain.repository.NotificationHistoryRepository;
import com.dnd5.timoapi.global.infrastructure.fcm.FcmMessage;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final String NOTIFICATION_TITLE = "회고 시간이에요";
    private static final String NOTIFICATION_BODY = "오늘 하루를 돌아보며 회고를 작성해보세요.";

    private final AlarmSettingRepository alarmSettingRepository;
    private final FcmService fcmService;
    private final NotificationHistoryRepository notificationHistoryRepository;

    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledNotifications() {
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<AlarmSettingEntity> settings = alarmSettingRepository.findAllByAlarmTimeAndDeletedAtIsNull(
                now);

        if (settings.isEmpty()) {
            return;
        }

        log.info("Sending scheduled notifications for time: {}, count: {}", now, settings.size());

        for (AlarmSettingEntity setting : settings) {
            try {
                fcmService.sendToUser(setting.getUserId(),
                        FcmMessage.of(NOTIFICATION_TITLE, NOTIFICATION_BODY));
                notificationHistoryRepository.save(
                        NotificationHistoryEntity.from(
                                new NotificationHistory(
                                        null,
                                        setting.getUserId(),
                                        NOTIFICATION_TITLE,
                                        NOTIFICATION_BODY,
                                        false,
                                        null
                                )
                        )
                );
            } catch (Exception e) {
                log.error("Failed to send notification to userId: {}", setting.getUserId(), e);
            }
        }
    }
}
