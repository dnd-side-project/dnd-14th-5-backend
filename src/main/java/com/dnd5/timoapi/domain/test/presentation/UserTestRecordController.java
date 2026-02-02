package com.dnd5.timoapi.domain.test.presentation;

import com.dnd5.timoapi.domain.test.application.service.UserTestRecordService;
import com.dnd5.timoapi.domain.test.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordDetailResponse;
import com.dnd5.timoapi.domain.test.presentation.response.UserTestRecordResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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

import java.util.List;

@RestController
@RequestMapping("/test-records")
@RequiredArgsConstructor
@Validated
public class UserTestRecordController {

    private final UserTestRecordService userTestRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserTestRecordCreateResponse create(
            @Valid @RequestBody UserTestRecordCreateRequest request
    ) {
        return userTestRecordService.create(request);
    }

    @PatchMapping("/{testRecordId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(
            @Positive @PathVariable Long testRecordId
    ) {
        userTestRecordService.complete(testRecordId);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public List<UserTestRecordResponse> findAll() {
        return userTestRecordService.findAll();
    }

    @GetMapping("/{testRecordId}")
    @ResponseStatus(HttpStatus.OK)
    public UserTestRecordDetailResponse findById(
            @Positive @PathVariable Long testRecordId
    ) {
        return userTestRecordService.findById(testRecordId);
    }

}
