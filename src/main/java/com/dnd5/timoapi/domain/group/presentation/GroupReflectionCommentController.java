package com.dnd5.timoapi.domain.group.presentation;

import com.dnd5.timoapi.domain.group.application.service.GroupReflectionCommentService;
import com.dnd5.timoapi.domain.group.presentation.request.GroupReflectionCommentRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupReflectionCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups/{groupId}/reflections/{reflectionId}/comments")
@RequiredArgsConstructor
@Validated
public class GroupReflectionCommentController {

    private final GroupReflectionCommentService groupReflectionCommentService;

    @Operation(summary = "그룹 멤버 회고 댓글 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @Positive @PathVariable Long groupId,
            @Positive @PathVariable Long reflectionId,
            @Valid @RequestBody GroupReflectionCommentRequest request) {
        groupReflectionCommentService.create(groupId, reflectionId, request);
    }

    @Operation(summary = "그룹 멤버 회고 댓글 조회")
    @GetMapping
    public List<GroupReflectionCommentResponse> findAll(
            @Positive @PathVariable Long groupId,
            @Positive @PathVariable Long reflectionId) {
        return groupReflectionCommentService.findAll(groupId, reflectionId);
    }

    @Operation(summary = "그룹 멤버 회고 댓글 수정")
    @PutMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long groupId,
            @Positive @PathVariable Long reflectionId,
            @Positive @PathVariable Long commentId,
            @Valid @RequestBody GroupReflectionCommentRequest request) {
        groupReflectionCommentService.update(groupId, reflectionId, commentId, request);
    }

    @Operation(summary = "그룹 멤버 회고 댓글 삭제")
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Positive @PathVariable Long groupId,
            @Positive @PathVariable Long reflectionId,
            @Positive @PathVariable Long commentId) {
        groupReflectionCommentService.delete(groupId, reflectionId, commentId);
    }
}
