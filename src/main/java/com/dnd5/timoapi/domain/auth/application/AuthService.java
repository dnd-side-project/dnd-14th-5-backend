package com.dnd5.timoapi.domain.auth.application;

import com.dnd5.timoapi.domain.auth.domain.repository.RefreshTokenRepository;
import com.dnd5.timoapi.domain.auth.exception.AuthErrorCode;
import io.jsonwebtoken.Claims;
import com.dnd5.timoapi.domain.auth.presentation.response.TokenResponse;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import com.dnd5.timoapi.global.security.jwt.JwtTokenExtractor;
import com.dnd5.timoapi.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExtractor jwtTokenExtractor;

    public TokenResponse login(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return issueTokens(user.getId(), user.getEmail(), user.getRole().name());
    }

    public TokenResponse reissue(String refreshToken) {
        Claims claims = jwtTokenExtractor.parseClaims(refreshToken);
        Long userId = jwtTokenExtractor.getUserId(claims);
        String storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.equals(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return issueTokens(user.getId(), user.getEmail(), user.getRole().name());
    }

    public void logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        refreshTokenRepository.deleteByUserId(userId);
    }

    private TokenResponse issueTokens(Long userId, String email, String role) {
        String accessToken = jwtTokenProvider.createAccessToken(userId, email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenRepository.save(userId, refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }
}
