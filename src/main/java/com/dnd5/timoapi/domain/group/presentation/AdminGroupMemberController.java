package com.dnd5.timoapi.domain.group.presentation;

import com.dnd5.timoapi.domain.group.application.service.AdminGroupMemberService;
import com.dnd5.timoapi.domain.user.presentation.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/groups")
@RequiredArgsConstructor
@Validated
public class AdminGroupMemberController {

    private final AdminGroupMemberService adminGroupMemberService;

    @Operation(summary = "그룹 멤버 전체 조회")
    @GetMapping("/{groupId}")
    public List<UserResponse> getAllMembersByGroup(@Positive @PathVariable Long groupId) {
        return adminGroupMemberService.getMembers(groupId);
    }

    @Operation(summary = "그룹 멤버 추방 (어드민)")
    @DeleteMapping("/{groupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long groupId, @Positive @PathVariable Long userId) {
        adminGroupMemberService.removeMember(groupId, userId);
    }
}
