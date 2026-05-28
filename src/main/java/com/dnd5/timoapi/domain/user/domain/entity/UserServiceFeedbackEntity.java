package com.dnd5.timoapi.domain.user.domain.entity;

import com.dnd5.timoapi.domain.user.domain.model.UserServiceFeedback;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "user_service_feedbacks")
public class UserServiceFeedbackEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Long serviceRating;

    @Column(nullable = false)
    private String serviceFeedback;

    public static UserServiceFeedbackEntity from(
            UserEntity user,
            Long serviceRating,
            String serviceFeedback
    ) {
        return new UserServiceFeedbackEntity(
                user,
                serviceRating,
                serviceFeedback
        );
    }

    public UserServiceFeedback toModel() {
        return new UserServiceFeedback(
                getId(),
                user.getId(),
                getServiceRating(),
                getServiceFeedback(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }

}
