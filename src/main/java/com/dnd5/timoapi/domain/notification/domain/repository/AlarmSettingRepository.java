package com.dnd5.timoapi.domain.notification.domain.repository;

import com.dnd5.timoapi.domain.notification.domain.entity.AlarmSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AlarmSettingRepository extends JpaRepository<AlarmSettingEntity, Long> {

    Optional<AlarmSettingEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<AlarmSettingEntity> findAllByAlarmTime(LocalTime alarmTime);
}
