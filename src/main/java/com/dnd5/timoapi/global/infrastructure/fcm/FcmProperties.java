package com.dnd5.timoapi.global.infrastructure.fcm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fcm")
public record FcmProperties(boolean enabled, String credentialsPath) {}
