package com.dnd5.timoapi.domain.auth.presentation;

import com.dnd5.timoapi.domain.auth.application.service.AuthService;
import com.dnd5.timoapi.domain.auth.presentation.request.LoginRequest;
import com.dnd5.timoapi.domain.auth.presentation.request.ReissueRequest;
import com.dnd5.timoapi.domain.auth.presentation.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/test-auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email());
    }

    @PostMapping("/test-auth/reissue")
    public TokenResponse reissue(@Valid @RequestBody ReissueRequest request) {
        return authService.reissue(request.refreshToken());
    }

    @PostMapping("/auth/logout")
    public void logout() {
        authService.logout();
    }
}
