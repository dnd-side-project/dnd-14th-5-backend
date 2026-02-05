package com.dnd5.timoapi.domain.auth.infrastructure.dto;

import com.dnd5.timoapi.domain.auth.exception.AuthErrorCode;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.model.User;
import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import com.dnd5.timoapi.global.exception.BusinessException;
import java.util.Map;
import lombok.Builder;

@Builder
public record OAuth2UserInfo(
        String name,
        String email,
        String profile,
        OAuthProvider provider
) {

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> ofGoogle(attributes);
            default -> throw new BusinessException(AuthErrorCode.ILLEGAL_REGISTRATION_ID);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .profile((String) attributes.get("picture"))
                .provider(OAuthProvider.GOOGLE)
                .build();
    }

    public UserEntity toEntity() {
        User user = User.create(email, name, "Asia/Seoul", provider);
        return UserEntity.from(user);
    }
}
