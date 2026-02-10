package com.dnd5.timoapi.domain.fcm.domain.entity;

import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "fcm_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "token"}),
        indexes = @Index(name = "idx_fcm_user_id", columnList = "user_id")
)
public class FcmTokenEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    public FcmTokenEntity(Long userId, String token, String deviceType) {
        this.userId = userId;
        this.token = token;
        this.deviceType = deviceType;
    }
}
