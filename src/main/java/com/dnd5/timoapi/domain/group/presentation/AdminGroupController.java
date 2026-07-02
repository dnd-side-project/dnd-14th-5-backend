package com.dnd5.timoapi.domain.group.presentation;

import com.dnd5.timoapi.domain.group.application.service.AdminGroupService;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/groups")
@RequiredArgsConstructor
@Validated
public class AdminGroupController {

    private final AdminGroupService adminGroupService;

    @Operation(summary = "그룹 생성 (어드민)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCreateResponse create(@Valid @RequestBody GroupCreateRequest request) {
        return adminGroupService.createGroup(request);
    }

    @Operation(summary = "더미 그룹 시드 데이터 생성 (어드민)")
    @PostMapping("/seed")
    @ResponseStatus(HttpStatus.CREATED)
    public void seed() {
        adminGroupService.seedDummyGroups();
    }

    @Operation(summary = "그룹 삭제 (어드민)")
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long groupId) {
        adminGroupService.deleteGroup(groupId);
    }
}
