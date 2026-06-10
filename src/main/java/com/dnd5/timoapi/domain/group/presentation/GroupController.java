package com.dnd5.timoapi.domain.group.presentation;

import com.dnd5.timoapi.domain.group.application.service.GroupService;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.request.GroupUpdateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupDetailResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupResponse;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupReflectionSort;
import com.dnd5.timoapi.domain.group.presentation.response.GroupTodayReflectionItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Validated
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCreateResponse createGroup(@Valid @RequestBody GroupCreateRequest request) {
        return groupService.createGroup(request);
    }

    @GetMapping
    public List<GroupResponse> getGroups(@RequestParam(required = false) String code) {
        if (code != null) {
            return List.of(groupService.getGroupByCode(code));
        }
        return groupService.getMyGroups();
    }

    @GetMapping("/{groupId}")
    public GroupDetailResponse getGroup(
            @Positive @PathVariable Long groupId,
            @RequestParam(required = false) String code) {
        return groupService.getGroup(groupId, code);
    }

    @PatchMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateGroup(
            @Positive @PathVariable Long groupId,
            @Valid @RequestBody GroupUpdateRequest request) {
        groupService.updateGroup(groupId, request);
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@Positive @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public void joinGroup(@Positive @PathVariable Long groupId) {
        groupService.joinGroup(groupId);
    }

    @DeleteMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(@Positive @PathVariable Long groupId) {
        groupService.leaveGroup(groupId);
    }

    @GetMapping("/{groupId}/reflections/today")
    public List<GroupTodayReflectionItem> getTodayReflections(
            @Positive @PathVariable Long groupId,
            @RequestParam(defaultValue = "LATEST") GroupReflectionSort sort) {
        return groupService.getTodayReflections(groupId, sort);
    }
}
