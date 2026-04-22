package com.dnd5.timoapi.global.analytics.event;

public record FeedbackReceivedEvent(Long userId, Long reflectionId, int score) {
}
