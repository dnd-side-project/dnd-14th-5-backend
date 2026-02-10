package com.dnd5.timoapi.global.security.cookie;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {

    private boolean secure = true;
    private String sameSite = "None";
    private String domain = "";
    private String path = "/";
}
