package com.dnd5.timoapi.domain.auth.infrastructure.handler;

import com.dnd5.timoapi.domain.auth.application.service.AuthService;
import com.dnd5.timoapi.domain.auth.infrastructure.config.OAuthProperties;
import com.dnd5.timoapi.domain.auth.infrastructure.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuthProperties oAuthProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        authService.issueTokens(
                oAuth2User.user().id(),
                oAuth2User.user().email(),
                oAuth2User.user().role().name(),
                response
        );

        response.sendRedirect(oAuthProperties.getRedirectUri());
    }
}
