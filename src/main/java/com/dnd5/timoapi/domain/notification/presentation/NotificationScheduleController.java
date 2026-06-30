package com.dnd5.timoapi.domain.notification.presentation;

import com.dnd5.timoapi.domain.notification.application.service.NotificationScheduleService;
import com.dnd5.timoapi.domain.notification.presentation.request.CreateScheduleRequest;
import com.dnd5.timoapi.domain.notification.presentation.request.UpdateScheduleRequest;
import com.dnd5.timoapi.domain.notification.presentation.response.ScheduleResponse;
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

@RestController
@RequestMapping("/notifications/schedules")
@RequiredArgsConstructor
@Validated
public class NotificationScheduleController {

    private final NotificationScheduleService scheduleService;

    @Operation(summary = "알림 스케줄 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody CreateScheduleRequest request) {
        scheduleService.create(request.notificationTime(), request.token());
    }

    @Operation(summary = "내 알림 스케줄 조회")
    @GetMapping("/me")
    public ScheduleResponse getMy() {
        return scheduleService.getMy();
    }

    @Operation(summary = "테스트 알림 전송")
    @PostMapping("/test-send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void testSend() {
        scheduleService.testSend();
    }

    @Operation(summary = "알림 스케줄 수정")
    @PatchMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Positive @PathVariable Long scheduleId, @Valid @RequestBody UpdateScheduleRequest request) {
        scheduleService.update(scheduleId, request.notificationTime());
    }

    @Operation(summary = "알림 스케줄 삭제")
    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long scheduleId) {
        scheduleService.delete(scheduleId);
    }
}
