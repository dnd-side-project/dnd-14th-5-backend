package com.dnd5.timoapi.domain.test.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ZtpiCategory {
    PAST_NEGATIVE(
            2.10,
            "그늘이",
            "경계주의자",
            "지나간 상처와 실수를 쉽게 잊지 않아요. 같은 일이 반복되지 않도록 조심하고 선택 앞에서 신중해지는 편이에요."
    ),
    PAST_POSITIVE(
            3.67,
            "추억이",
            "회상주의자",
            "좋았던 기억을 소중히 간직해요. 지나온 시간 속에서 의미를 찾고 그 경험을 지금의 기준으로 삼아요."
    ),
    PRESENT_HEDONISTIC(
            4.33,
            "지금이",
            "현실주의자",
            "지금 이 순간을 가장 중요하게 생각해요. 불필요한 걱정보다는, 현재에 집중할 때 마음이 편해져요."
    ),
    PRESENT_FATALISTIC(
            1.67,
            "담담이",
            "운명주의자",
            "인생에는 내가 바꿀 수 없는 흐름도 있다고 느껴요. 억지로 거스르기보다, 주어진 상황을 담담하게 받아들이는 편이에요."
    ),
    FUTURE(
            3.69,
            "내일이",
            "계획주의자",
            "지금의 선택이 미래를 만든다고 믿어요. 당장의 즐거움보다, 장기적인 목표와 계획을 더 중요하게 생각해요."
    );

    private final double idealScore;
    private final String character;
    private final String personality;
    private final String description;
}
