package com.dnd5.timoapi;

import com.dnd5.timoapi.global.infrastructure.file.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties(FileStorageProperties.class)
@SpringBootApplication
public class TimoBackendApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimoBackendApiApplication.class, args);
    }

}
