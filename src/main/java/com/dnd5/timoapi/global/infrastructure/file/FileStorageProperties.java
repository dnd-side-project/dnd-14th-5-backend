package com.dnd5.timoapi.global.infrastructure.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public record FileStorageProperties(String dir, String baseUrl) {
}
