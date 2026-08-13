package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionLikeEntity;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionLikeRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionPrivateRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupReflectionLikeServiceTest {

    @InjectMocks
    private GroupReflectionLikeService groupReflectionLikeService;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private GroupMemberReflectionPrivateRepository groupMemberReflectionPrivateRepository;

    @Mock
    private GroupMemberReflectionLikeRepository groupMemberReflectionLikeRepository;

    @Test
    void like_성공() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(authorId);
        when(reflection.getDate()).thenReturn(LocalDate.of(2026, 8, 1));
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflection));

        GroupMemberEntity authorMember = mock(GroupMemberEntity.class);
        when(authorMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, authorId))
                .thenReturn(Optional.of(authorMember));

        when(groupMemberReflectionPrivateRepository.existsByGroupIdAndReflectionId(groupId, reflectionId)).thenReturn(false);
        when(groupMemberReflectionLikeRepository.existsByGroupIdAndReflectionIdAndUserId(groupId, reflectionId, viewerId))
                .thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            groupReflectionLikeService.like(groupId, reflectionId);

            verify(groupMemberReflectionLikeRepository).save(any(GroupMemberReflectionLikeEntity.class));
        }
    }

    @Test
    void like_비멤버_403() {
        Long viewerId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionLikeService.like(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void like_비공개_회고는_타인이_불가() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(authorId);
        when(reflection.getDate()).thenReturn(LocalDate.of(2026, 8, 1));
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflection));

        GroupMemberEntity authorMember = mock(GroupMemberEntity.class);
        when(authorMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, authorId))
                .thenReturn(Optional.of(authorMember));

        when(groupMemberReflectionPrivateRepository.existsByGroupIdAndReflectionId(groupId, reflectionId)).thenReturn(true);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionLikeService.like(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void like_이미_좋아요면_409() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(authorId);
        when(reflection.getDate()).thenReturn(LocalDate.of(2026, 8, 1));
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflection));

        GroupMemberEntity authorMember = mock(GroupMemberEntity.class);
        when(authorMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, authorId))
                .thenReturn(Optional.of(authorMember));

        when(groupMemberReflectionPrivateRepository.existsByGroupIdAndReflectionId(groupId, reflectionId)).thenReturn(false);
        when(groupMemberReflectionLikeRepository.existsByGroupIdAndReflectionIdAndUserId(groupId, reflectionId, viewerId))
                .thenReturn(true);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionLikeService.like(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_REFLECTION_LIKE_ALREADY_EXISTS));
        }
    }

    @Test
    void like_작성자_그룹_가입일_이전_회고면_404() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(authorId);
        when(reflection.getDate()).thenReturn(LocalDate.of(2026, 6, 1));
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflection));

        GroupMemberEntity authorMember = mock(GroupMemberEntity.class);
        when(authorMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, authorId))
                .thenReturn(Optional.of(authorMember));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionLikeService.like(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ReflectionErrorCode.REFLECTION_NOT_FOUND));
        }
    }

    @Test
    void unlike_성공() {
        Long viewerId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        GroupMemberReflectionLikeEntity like = mock(GroupMemberReflectionLikeEntity.class);
        when(groupMemberReflectionLikeRepository.findByGroupIdAndReflectionIdAndUserId(groupId, reflectionId, viewerId))
                .thenReturn(Optional.of(like));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            groupReflectionLikeService.unlike(groupId, reflectionId);

            verify(groupMemberReflectionLikeRepository).delete(like);
        }
    }

    @Test
    void unlike_비멤버_403() {
        Long viewerId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionLikeService.unlike(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void unlike_좋아요_없으면_404() {
        Long viewerId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);
        when(groupMemberReflectionLikeRepository.findByGroupIdAndReflectionIdAndUserId(groupId, reflectionId, viewerId))
                .thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionLikeService.unlike(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_REFLECTION_LIKE_NOT_FOUND));
        }
    }
}
