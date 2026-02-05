package com.dnd5.timoapi.domain.auth.infrastructure.handler;

import com.dnd5.timoapi.domain.auth.infrastructure.config.OAuthProperties;
import com.dnd5.timoapi.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final OAuthProperties oAuthProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        String redirectUrl = UriComponentsBuilder.fromUriString(oAuthProperties.getRedirectUri())
                .fragment("accessToken=" + accessToken + "&refreshToken=" + refreshToken)
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
