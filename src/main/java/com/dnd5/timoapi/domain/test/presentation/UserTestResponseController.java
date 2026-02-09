package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.UserTestResponseService;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestResponseCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestResponseUpdateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestResponseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-records/{testRecordId}/responses")
@RequiredArgsConstructor
@Validated
public class UserTestResponseController {

    private final UserTestResponseService userTestResponseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @Positive @PathVariable Long testRecordId,
            @Valid @RequestBody UserTestResponseCreateRequest request
    ) {
        userTestResponseService.create(testRecordId, request);
    }

    @PatchMapping("/{responseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @Positive @PathVariable Long testRecordId,
            @Positive @PathVariable Long responseId,
            @Valid @RequestBody UserTestResponseUpdateRequest request
    ) {
        userTestResponseService.update(testRecordId, responseId, request);
    }

    @GetMapping
    public List<UserTestResponseResponse> findAll(@Positive @PathVariable Long testRecordId) {
        return userTestResponseService.findAll(testRecordId);
    }

    @GetMapping("/{responseId}")
    public UserTestResponseResponse findById(
            @Positive @PathVariable Long testRecordId,
            @Positive @PathVariable Long responseId
    ) {
        return userTestResponseService.findById(testRecordId, responseId);
    }

}
