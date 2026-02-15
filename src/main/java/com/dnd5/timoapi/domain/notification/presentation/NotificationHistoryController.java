package com.dnd5.timoapi.domain.notification.presentation;

import com.dnd5.timoapi.domain.notification.application.service.NotificationHistoryService;
import com.dnd5.timoapi.domain.notification.presentation.response.NotificationHistoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications/histories")
@RequiredArgsConstructor
@Validated
public class NotificationHistoryController {

    private final NotificationHistoryService notificationHistoryService;

    @GetMapping("/me")
    public List<NotificationHistoryResponse> getMy() {
        return notificationHistoryService.getMyNotificationHistory();
    }

    @PatchMapping("/{historyId}")
    public void update(@PathVariable Long historyId) {
        notificationHistoryService.updateNotificationHistory(historyId);
    }
}
