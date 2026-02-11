package com.dnd5.timoapi.domain.notification.presentation.response;

import com.dnd5.timoapi.domain.notification.domain.entity.AlarmSettingEntity;

import java.time.LocalTime;

public record ScheduleResponse(
        Long id,
        LocalTime notificationTime
) {

    public static ScheduleResponse from(AlarmSettingEntity entity) {
        return new ScheduleResponse(entity.getId(), entity.getAlarmTime());
    }
}
