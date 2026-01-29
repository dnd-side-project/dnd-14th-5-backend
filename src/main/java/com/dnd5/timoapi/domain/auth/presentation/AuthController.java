package com.dnd5.timoapi.domain.auth.presentation;

import com.dnd5.timoapi.domain.auth.application.AuthService;
import com.dnd5.timoapi.domain.auth.presentation.request.LoginRequest;
import com.dnd5.timoapi.domain.auth.presentation.request.ReissueRequest;
import com.dnd5.timoapi.domain.auth.presentation.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.provider());
    }

    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody ReissueRequest request) {
        return authService.reissue(request.refreshToken());
    }

    @PostMapping("/logout")
    public void logout() {
        authService.logout();
    }
}
