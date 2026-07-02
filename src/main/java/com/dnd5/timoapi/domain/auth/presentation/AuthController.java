package com.dnd5.timoapi.domain.auth.presentation;

import com.dnd5.timoapi.domain.auth.application.service.AuthService;
import com.dnd5.timoapi.domain.auth.presentation.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "테스트 로그인")
    @PostMapping("/test-auth/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        authService.login(request.email(), response);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/auth/reissue")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reissue(@CookieValue("refresh_token") String refreshToken, HttpServletResponse response) {
        authService.reissue(refreshToken, response);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        authService.logout(response);
    }
}
