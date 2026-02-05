package com.dnd5.timoapi.domain.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private String redirectUri = "/test.html";
    private Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Cookie {
        private String domain = "";
        private String sameSite = "Lax";
    }
}
