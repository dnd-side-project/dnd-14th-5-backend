package com.dnd5.timoapi.domain.user.presentation;

import com.dnd5.timoapi.domain.user.application.service.UserTestRecordService;
import com.dnd5.timoapi.domain.user.presentation.request.UserTestRecordCreateRequest;
import com.dnd5.timoapi.domain.user.presentation.response.UserTestRecordCreateResponse;
import com.dnd5.timoapi.domain.user.presentation.response.UserTestRecordDetailResponse;
import com.dnd5.timoapi.domain.user.presentation.response.UserTestRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Operation(summary = "테스트 기록 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserTestRecordCreateResponse create(
            @Valid @RequestBody UserTestRecordCreateRequest request
    ) {
        return userTestRecordService.create(request);
    }

    @Operation(summary = "테스트 기록 완료")
    @PatchMapping("/{testRecordId}/complete")
    public UserTestRecordDetailResponse complete(
            @Positive @PathVariable Long testRecordId
    ) {
        return userTestRecordService.complete(testRecordId);
    }

    @Operation(summary = "내 테스트 기록 목록 조회")
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public List<UserTestRecordResponse> findAll() {
        return userTestRecordService.findAll();
    }

    @Operation(summary = "테스트 기록 단건 조회")
    @GetMapping("/{testRecordId}")
    @ResponseStatus(HttpStatus.OK)
    public UserTestRecordDetailResponse findById(
            @Positive @PathVariable Long testRecordId
    ) {
        return userTestRecordService.findById(testRecordId);
    }

    @Operation(summary = "테스트 기록 삭제")
    @DeleteMapping("/{testRecordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Positive @PathVariable Long testRecordId
    ) {
        userTestRecordService.delete(testRecordId);
    }

}
