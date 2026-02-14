package com.dnd5.timoapi.domain.notification.domain.repository;

import com.dnd5.timoapi.domain.notification.domain.entity.NotificationHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends
        JpaRepository<NotificationHistoryEntity, Long> {
    List<NotificationHistoryEntity> findAllByIsReadFalse();
}
