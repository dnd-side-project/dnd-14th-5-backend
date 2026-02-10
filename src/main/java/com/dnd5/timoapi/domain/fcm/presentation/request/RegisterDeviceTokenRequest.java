package com.dnd5.timoapi.domain.fcm.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceTokenRequest(
        @NotBlank String token,
        String deviceType
) {}
