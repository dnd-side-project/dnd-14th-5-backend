package com.dnd5.timoapi.global.analytics.domain.repository;

import com.dnd5.timoapi.global.analytics.domain.entity.AnalyticsEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEventEntity, Long> {
}
