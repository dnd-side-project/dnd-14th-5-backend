package com.dnd5.timoapi.global.infrastructure.notification;

public interface Notifier {
    void notify(NotificationType type, ErrorNotificationData data);
}
