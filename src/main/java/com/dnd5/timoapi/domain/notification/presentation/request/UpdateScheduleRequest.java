package com.dnd5.timoapi.domain.notification.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record UpdateScheduleRequest(
        @NotNull LocalTime notificationTime
) {}
