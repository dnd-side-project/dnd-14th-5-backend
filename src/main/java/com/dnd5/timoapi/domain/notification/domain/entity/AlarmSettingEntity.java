package com.dnd5.timoapi.domain.notification.domain.entity;

import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "users_reflection_alarm_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id"),
        indexes = @Index(name = "idx_alarm_user_id", columnList = "user_id")
)
public class AlarmSettingEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "alarm_time", nullable = false)
    private LocalTime alarmTime;

    public void updateAlarmTime(LocalTime alarmTime) {
        if (alarmTime != null) this.alarmTime = alarmTime;
    }
}
