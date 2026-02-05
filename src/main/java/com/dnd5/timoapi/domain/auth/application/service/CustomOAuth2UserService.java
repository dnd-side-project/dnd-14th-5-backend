package com.dnd5.timoapi.domain.auth.application.service;

import com.dnd5.timoapi.domain.auth.infrastructure.dto.CustomOAuth2User;
import com.dnd5.timoapi.domain.auth.infrastructure.dto.OAuth2UserInfo;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.model.User;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

        User user = getOrSaveUser(oAuth2UserInfo);
        return new CustomOAuth2User(user, oAuth2UserAttributes, userNameAttributeName);
    }

    private User getOrSaveUser(OAuth2UserInfo info) {
        return userRepository.findByEmailAndDeletedAtIsNull(info.email())
                .map(UserEntity::toModel)
                .orElseGet(() -> userRepository.save(info.toEntity()).toModel());
    }
}
