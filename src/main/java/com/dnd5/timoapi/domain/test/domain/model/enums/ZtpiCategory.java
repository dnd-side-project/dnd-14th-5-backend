package com.dnd5.timoapi.domain.test.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ZtpiCategory {
    PAST_NEGATIVE(2.10),
    PAST_POSITIVE(3.67),
    PRESENT_HEDONISTIC(4.33),
    PRESENT_FATALISTIC(1.67),
    FUTURE(3.69);

    private final double idealScore;
}
