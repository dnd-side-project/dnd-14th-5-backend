package com.dnd5.timoapi.domain.user.presentation;

import com.dnd5.timoapi.domain.user.application.service.UserService;
import com.dnd5.timoapi.domain.user.presentation.response.AdminUserDetailResponse;
import com.dnd5.timoapi.domain.user.presentation.response.AdminUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "전체 유저 목록 조회 (어드민)")
    @GetMapping
    public List<AdminUserResponse> getUsers() {
        return userService.findAllForAdmin();
    }

    @Operation(summary = "유저 상세 조회 (어드민)")
    @GetMapping("/{userId}")
    public AdminUserDetailResponse getUser(@Positive @PathVariable Long userId) {
        return userService.findByIdForAdmin(userId);
    }
}
