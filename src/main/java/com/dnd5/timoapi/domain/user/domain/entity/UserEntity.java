package com.dnd5.timoapi.domain.user.domain.entity;

import com.dnd5.timoapi.domain.user.domain.model.User;
import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import com.dnd5.timoapi.domain.user.domain.model.enums.UserRole;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private Boolean isOnboarded = false;

    @Column(nullable = false)
    private Integer streakDays = 0;

    public static UserEntity from(User model) {
        return new UserEntity(model.email(), model.nickname(), model.timezone(), model.provider(), model.role(), model.isOnboarded(), model.streakDays());
    }

    public User toModel() {
        return new User(getId(), email, nickname, timezone, provider, role, isOnboarded, streakDays, getCreatedAt(), getUpdatedAt());
    }

    public void update(String nickname) {
        if (nickname != null) this.nickname = nickname;
    }

    public void completeOnboarding() {
        this.isOnboarded = true;
    }

    public void incrementStreakDays() {
        this.streakDays += 1;
    }

    public void resetStreakDays() {
        this.streakDays = 0;
    }

    public void restore() {
        setDeletedAt(null);
    }
}
