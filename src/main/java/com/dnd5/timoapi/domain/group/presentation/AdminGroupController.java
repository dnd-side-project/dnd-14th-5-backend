package com.dnd5.timoapi.domain.group.presentation;

import com.dnd5.timoapi.domain.group.application.service.AdminGroupService;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCreateResponse create(@Valid @RequestBody GroupCreateRequest request) {
        return adminGroupService.createGroup(request);
    }

    @PostMapping("/seed")
    @ResponseStatus(HttpStatus.CREATED)
    public void seed() {
        adminGroupService.seedDummyGroups();
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long groupId) {
        adminGroupService.deleteGroup(groupId);
    }
}
