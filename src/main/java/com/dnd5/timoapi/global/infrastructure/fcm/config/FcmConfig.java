package com.dnd5.timoapi.global.infrastructure.fcm.config;

import com.dnd5.timoapi.global.infrastructure.fcm.FcmProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(FcmProperties.class)
@RequiredArgsConstructor
public class FcmConfig {

    private final FcmProperties properties;

    @PostConstruct
    public void initialize() {
        if (!properties.enabled() || properties.credentialsPath() == null || properties.credentialsPath().isBlank()) {
            log.info("FCM disabled or credentials not configured");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(properties.credentialsPath())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }
}
