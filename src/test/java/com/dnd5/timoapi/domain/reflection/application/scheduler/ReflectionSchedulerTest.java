package com.dnd5.timoapi.domain.reflection.application.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.application.support.TodayQuestionResolver;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.UserReflectionQuestionOrderEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.UserReflectionQuestionOrderRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReflectionSchedulerTest {

    @InjectMocks
    private ReflectionScheduler reflectionScheduler;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private ReflectionQuestionRepository reflectionQuestionRepository;

    @Mock
    private UserReflectionQuestionOrderRepository userReflectionQuestionOrderRepository;

    @Mock
    private TodayQuestionResolver todayQuestionResolver;

    @Mock
    private UserRepository userRepository;

    @Test
    void processYesterdayReflections_탈퇴유저_제외조회_사용() {
        Long activeUserId = 1L;
        Long inactiveUserId = 2L;
        Long yesterdayQuestionId = 11L;
        Long todayQuestionId = 22L;

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(activeUserId);
        when(reflection.getQuestionId()).thenReturn(yesterdayQuestionId);
        when(reflectionRepository.findAllByDateAndUserDeletedAtIsNull(any(LocalDate.class)))
                .thenReturn(List.of(reflection));

        ReflectionQuestionEntity yesterdayQuestion = mock(ReflectionQuestionEntity.class);
        when(yesterdayQuestion.getCategory()).thenReturn(ZtpiCategory.FUTURE);
        when(reflectionQuestionRepository.findById(yesterdayQuestionId))
                .thenReturn(Optional.of(yesterdayQuestion));

        UserReflectionQuestionOrderEntity order = mock(UserReflectionQuestionOrderEntity.class);
        when(userReflectionQuestionOrderRepository.findByUserIdAndCategory(activeUserId, ZtpiCategory.FUTURE))
                .thenReturn(Optional.of(order));

        when(todayQuestionResolver.resolveTodayCategory(activeUserId)).thenReturn(ZtpiCategory.FUTURE);
        when(todayQuestionResolver.resolveTodaySequence(activeUserId, ZtpiCategory.FUTURE)).thenReturn(1L);

        ReflectionQuestionEntity todayQuestion = mock(ReflectionQuestionEntity.class);
        when(todayQuestion.getId()).thenReturn(todayQuestionId);
        when(reflectionQuestionRepository.findBySequenceAndCategory(1L, ZtpiCategory.FUTURE))
                .thenReturn(Optional.of(todayQuestion));

        UserEntity activeUser = mock(UserEntity.class);
        UserEntity inactiveUser = mock(UserEntity.class);
        when(activeUser.getId()).thenReturn(activeUserId);
        when(inactiveUser.getId()).thenReturn(inactiveUserId);
        when(userRepository.findAllByStreakDaysGreaterThanAndDeletedAtIsNull(0))
                .thenReturn(List.of(activeUser, inactiveUser));

        reflectionScheduler.processYesterdayReflections();

        verify(reflectionRepository).findAllByDateAndUserDeletedAtIsNull(any(LocalDate.class));
        verify(reflectionRepository, never()).findAllByDate(any(LocalDate.class));
        verify(order).incrementSequence();
        verify(todayQuestionResolver).cacheQuestionId(activeUserId, todayQuestionId);
        verify(inactiveUser).resetStreakDays();
        verify(activeUser, never()).resetStreakDays();
    }
}
