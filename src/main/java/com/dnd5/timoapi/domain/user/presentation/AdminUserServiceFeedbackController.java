package com.dnd5.timoapi.domain.user.presentation;

import com.dnd5.timoapi.domain.user.application.service.UserServiceFeedbackService;
import com.dnd5.timoapi.domain.user.presentation.response.UserServiceFeedbackDetailResponse;
import com.dnd5.timoapi.domain.user.presentation.response.UserServiceFeedbackResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/service-feedbacks")
@RequiredArgsConstructor
@Validated
public class AdminUserServiceFeedbackController {

    private final UserServiceFeedbackService userServiceFeedbackService;

    @Operation(summary = "서비스 피드백 목록 조회 (어드민)")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserServiceFeedbackResponse> findAll() {
        return userServiceFeedbackService.findAll();
    }

    @Operation(summary = "서비스 피드백 단건 조회 (어드민)")
    @GetMapping("/{feedbackId}")
    @ResponseStatus(HttpStatus.OK)
    public UserServiceFeedbackDetailResponse findById(
            @Positive @PathVariable Long feedbackId
    ) {
        return userServiceFeedbackService.findById(feedbackId);
    }

    @Operation(summary = "서비스 피드백 삭제 (어드민)")
    @DeleteMapping("/{feedbackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Positive @PathVariable Long feedbackId
    ) {
        userServiceFeedbackService.delete(feedbackId);
    }

}
