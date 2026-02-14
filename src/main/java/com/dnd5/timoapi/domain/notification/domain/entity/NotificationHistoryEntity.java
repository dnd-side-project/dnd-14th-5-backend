package com.dnd5.timoapi.domain.notification.domain.entity;

import com.dnd5.timoapi.domain.notification.domain.model.NotificationHistory;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "notification_histories"
)
public class NotificationHistoryEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public static NotificationHistoryEntity from(NotificationHistory model) {
        return new NotificationHistoryEntity(
                model.userId(),
                model.title(),
                model.body(),
                model.isRead()
        );
    }

    public NotificationHistory toModel() {
        return new NotificationHistory(
                getId(),
                getUserId(),
                getTitle(),
                getBody(),
                isRead(),
                getCreatedAt());
    }

    public void read() {
        this.isRead = true;
    }
}
