package com.dnd5.timoapi.domain.user.presentation;

import com.dnd5.timoapi.domain.user.application.service.UserServiceFeedbackService;
import com.dnd5.timoapi.domain.user.application.service.UserTestRecordService;
import com.dnd5.timoapi.domain.user.presentation.request.UserServiceFeedbackCreateRequest;
import com.dnd5.timoapi.domain.user.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.user.presentation.response.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-feedbacks")
@RequiredArgsConstructor
@Validated
public class UserServiceFeedbackController {

    private final UserServiceFeedbackService userServiceFeedbackService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @Valid @RequestBody UserServiceFeedbackCreateRequest request
    ) {
        userServiceFeedbackService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserServiceFeedbackResponse> findAll() {
        return userServiceFeedbackService.findAll();
    }

    @GetMapping("/{feedbackId}")
    @ResponseStatus(HttpStatus.OK)
    public UserServiceFeedbackDetailResponse findById(
            @Positive @PathVariable Long feedbackId
    ) {
        return userServiceFeedbackService.findById(feedbackId);
    }

    @DeleteMapping("/{feedbackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Positive @PathVariable Long feedbackId
    ) {
        userServiceFeedbackService.delete(feedbackId);
    }

}
