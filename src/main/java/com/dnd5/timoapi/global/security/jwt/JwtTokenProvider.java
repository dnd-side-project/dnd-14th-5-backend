package com.dnd5.timoapi.global.security.jwt;

import com.dnd5.timoapi.domain.auth.infrastructure.dto.CustomOAuth2User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(Authentication authentication) {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        return createAccessToken(
                oAuth2User.user().id(),
                oAuth2User.user().email(),
                oAuth2User.user().role().name()
        );
    }

    public String createRefreshToken(Authentication authentication) {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        return createRefreshToken(oAuth2User.user().id());
    }

    public String createAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtProperties.getSecretKey())
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtProperties.getSecretKey())
                .compact();
    }
}
