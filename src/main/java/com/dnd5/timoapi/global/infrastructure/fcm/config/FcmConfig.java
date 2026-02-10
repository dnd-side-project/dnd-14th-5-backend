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
import java.io.InputStream;
import java.net.URI;

@Slf4j
@Configuration
@EnableConfigurationProperties(FcmProperties.class)
@RequiredArgsConstructor
public class FcmConfig {

    private final FcmProperties properties;

    @PostConstruct
    public void initialize() {
        if (!properties.enabled()) {
            log.info("FCM disabled");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            GoogleCredentials credentials;
            String path = properties.credentialsPath();

            if (path == null || path.isBlank()) {
                credentials = GoogleCredentials.getApplicationDefault();
            } else if (path.startsWith("http")) {
                credentials = GoogleCredentials.fromStream(URI.create(path).toURL().openStream());
            } else {
                credentials = GoogleCredentials.fromStream(new FileInputStream(path));
            }

            FirebaseApp.initializeApp(FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build());
            log.info("Firebase initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }
}
