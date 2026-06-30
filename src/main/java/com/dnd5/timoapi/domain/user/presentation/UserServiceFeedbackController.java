package com.dnd5.timoapi.domain.user.presentation;

import com.dnd5.timoapi.domain.user.application.service.UserServiceFeedbackService;
import com.dnd5.timoapi.domain.user.presentation.request.UserServiceFeedbackCreateRequest;
import com.dnd5.timoapi.domain.user.presentation.response.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service-feedbacks")
@RequiredArgsConstructor
@Validated
public class UserServiceFeedbackController {

    private final UserServiceFeedbackService userServiceFeedbackService;

    @Operation(summary = "서비스 피드백 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @Valid @RequestBody UserServiceFeedbackCreateRequest request
    ) {
        userServiceFeedbackService.create(request);
    }

}
