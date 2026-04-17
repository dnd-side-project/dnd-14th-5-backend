package com.dnd5.timoapi.global.analytics.event;

import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.util.Map;

public record TestCompletedEvent(Long userId, Long testRecordId, Map<ZtpiCategory, Double> scores) {
}
