package com.dnd5.timoapi.domain.test.domain.entity;

import com.dnd5.timoapi.domain.test.domain.model.UserTestResult;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
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
@Table(name = "user_test_record_results")
public class UserTestResultEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_record_id", nullable = false)
    private UserTestRecordEntity userTestRecord;

    @Column(nullable = false)
    private ZtpiCategory category;

    @Column(name = "score", nullable = false)
    private double score;

    public static UserTestResultEntity from(
            UserTestRecordEntity userTestRecord,
            ZtpiCategory category,
            double score
    ) {
        return new UserTestResultEntity(
                userTestRecord,
                category,
                score
        );
    }

    public UserTestResult toModel() {
        return new UserTestResult(
                getId(),
                userTestRecord.getId(),
                getCategory(),
                getScore(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }

}
