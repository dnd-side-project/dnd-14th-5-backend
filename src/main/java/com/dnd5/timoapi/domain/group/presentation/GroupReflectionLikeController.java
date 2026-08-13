package com.dnd5.timoapi.domain.group.presentation;

import com.dnd5.timoapi.domain.group.application.service.GroupReflectionLikeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups/{groupId}/reflections/{reflectionId}/like")
@RequiredArgsConstructor
@Validated
public class GroupReflectionLikeController {

    private final GroupReflectionLikeService groupReflectionLikeService;

    @Operation(summary = "그룹 멤버 회고 좋아요 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void like(@Positive @PathVariable Long groupId, @Positive @PathVariable Long reflectionId) {
        groupReflectionLikeService.like(groupId, reflectionId);
    }

    @Operation(summary = "그룹 멤버 회고 좋아요 삭제")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@Positive @PathVariable Long groupId, @Positive @PathVariable Long reflectionId) {
        groupReflectionLikeService.unlike(groupId, reflectionId);
    }
}
