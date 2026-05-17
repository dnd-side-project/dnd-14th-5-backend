package com.dnd5.timoapi.domain.test.domain.model.enums;

public enum TestType {
    ZTPI_15(15),
    ZTPI_56(56);

    private final int maxQuestionCount;

    TestType(int maxQuestionCount) {
        this.maxQuestionCount = maxQuestionCount;
    }

    public int getMaxQuestionCount() {
        return maxQuestionCount;
    }
}
