package com.dnd5.timoapi.domain.user.presentation;

import com.dnd5.timoapi.domain.user.application.service.UserServiceFeedbackService;
import com.dnd5.timoapi.domain.user.presentation.request.UserServiceFeedbackCreateRequest;
import com.dnd5.timoapi.domain.user.presentation.response.*;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @Valid @RequestBody UserServiceFeedbackCreateRequest request
    ) {
        userServiceFeedbackService.create(request);
    }

}
