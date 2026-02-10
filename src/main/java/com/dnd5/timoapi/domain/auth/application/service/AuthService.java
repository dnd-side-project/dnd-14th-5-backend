package com.dnd5.timoapi.domain.auth.application.service;

import com.dnd5.timoapi.domain.auth.domain.repository.RefreshTokenRepository;
import com.dnd5.timoapi.domain.auth.exception.AuthErrorCode;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import com.dnd5.timoapi.global.security.cookie.CookieUtil;
import com.dnd5.timoapi.global.security.jwt.JwtTokenExtractor;
import com.dnd5.timoapi.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final CookieUtil cookieUtil;

    public void login(String email, HttpServletResponse response) {
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        issueTokens(user.getId(), user.getEmail(), user.getRole().name(), response);
    }

    public void reissue(String refreshToken, HttpServletResponse response) {
        Claims claims = jwtTokenExtractor.parseClaims(refreshToken);
        Long userId = jwtTokenExtractor.getUserId(claims);
        String storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.equals(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        issueTokens(user.getId(), user.getEmail(), user.getRole().name(), response);
    }

    public void logout(HttpServletResponse response) {
        Long userId = SecurityUtil.getCurrentUserId();
        refreshTokenRepository.deleteByUserId(userId);
        cookieUtil.deleteCookies(response);
    }

    public void issueTokens(Long userId, String email, String role, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.createAccessToken(userId, email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenRepository.save(userId, refreshToken);

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
    }
}
