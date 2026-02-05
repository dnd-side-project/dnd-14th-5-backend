package com.dnd5.timoapi.domain.auth.infrastructure.dto;

import com.dnd5.timoapi.domain.user.domain.model.User;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public record CustomOAuth2User(
        User user,
        Map<String, Object> attributes,
        String attributeKey
) implements OAuth2User {

    @Override
    public String getName() {
        return attributes.get(attributeKey).toString();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.role().name()));
    }
}

