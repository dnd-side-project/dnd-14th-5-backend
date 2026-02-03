package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.UserTestRecord;
import com.dnd5.timoapi.domain.test.domain.model.enums.TestRecordStatus;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "user_test_records")
public class UserTestRecordEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestEntity test;

    @Column(nullable = false)
    private TestRecordStatus status;

    public static UserTestRecordEntity from(
            UserEntity user,
            TestEntity test,
            UserTestRecord model
    ) {
        return new UserTestRecordEntity(
                user,
                test,
                model.status()
        );
    }

    public UserTestRecord toModel() {
        return new UserTestRecord(
                getId(),
                user.getId(),
                test.getId(),
                getStatus(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public boolean isCompleted() {
        return this.status == TestRecordStatus.COMPLETED;
    }

    public void complete() {
        this.status = TestRecordStatus.COMPLETED;
    }

}
