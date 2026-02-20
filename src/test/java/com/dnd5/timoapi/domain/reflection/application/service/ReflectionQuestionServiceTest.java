package com.dnd5.timoapi.domain.reflection.application.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.infrastructure.cache.TodayQuestionCacheService;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReflectionQuestionServiceTest {

    @InjectMocks
    private ReflectionQuestionService reflectionQuestionService;

    @Mock
    private ReflectionQuestionRepository reflectionQuestionRepository;

    @Mock
    private TodayQuestionCacheService todayQuestionCacheService;

    @Test
    void delete_질문삭제시_관련_캐시_무효화() {
        Long questionId = 10L;
        Long deletedSequence = 2L;

        ReflectionQuestionEntity deletedQuestion = mock(ReflectionQuestionEntity.class);
        when(deletedQuestion.getId()).thenReturn(questionId);
        when(deletedQuestion.getCategory()).thenReturn(ZtpiCategory.FUTURE);
        when(deletedQuestion.getSequence()).thenReturn(deletedSequence);

        ReflectionQuestionEntity nextQuestion = mock(ReflectionQuestionEntity.class);

        when(reflectionQuestionRepository.findByIdAndDeletedAtIsNull(questionId))
                .thenReturn(Optional.of(deletedQuestion));
        when(reflectionQuestionRepository.findAllByCategoryAndSequenceGreaterThan(
                ZtpiCategory.FUTURE,
                deletedSequence
        )).thenReturn(List.of(nextQuestion));

        reflectionQuestionService.delete(questionId);

        verify(deletedQuestion).setSequence(-questionId);
        verify(nextQuestion).decreaseSequence();
        verify(todayQuestionCacheService).evictByQuestionId(questionId);
    }
}
