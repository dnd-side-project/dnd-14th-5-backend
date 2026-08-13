package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionCommentEntity;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionCommentRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionPrivateRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupReflectionCommentRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupReflectionCommentResponse;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupReflectionCommentServiceTest {

    @InjectMocks
    private GroupReflectionCommentService groupReflectionCommentService;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private GroupMemberReflectionPrivateRepository groupMemberReflectionPrivateRepository;

    @Mock
    private GroupMemberReflectionCommentRepository groupMemberReflectionCommentRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void create_성공() {
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

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            groupReflectionCommentService.create(groupId, reflectionId, new GroupReflectionCommentRequest("좋은 회고네요"));

            verify(groupMemberReflectionCommentRepository).save(any(GroupMemberReflectionCommentEntity.class));
        }
    }

    @Test
    void create_비공개_회고는_타인이_불가() {
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

            assertThatThrownBy(() -> groupReflectionCommentService.create(
                    groupId, reflectionId, new GroupReflectionCommentRequest("내용")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void findAll_닉네임_포함해_반환() {
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

        GroupMemberReflectionCommentEntity comment = mock(GroupMemberReflectionCommentEntity.class);
        when(comment.getId()).thenReturn(500L);
        when(comment.getUserId()).thenReturn(authorId);
        when(comment.getContent()).thenReturn("댓글 내용");
        when(comment.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 8, 2, 12, 0));
        when(groupMemberReflectionCommentRepository
                .findAllByGroupIdAndReflectionIdAndDeletedAtIsNullOrderByCreatedAtAsc(groupId, reflectionId))
                .thenReturn(List.of(comment));

        UserEntity commenter = mock(UserEntity.class);
        when(commenter.getId()).thenReturn(authorId);
        when(commenter.getNickname()).thenReturn("작성자닉네임");
        when(commenter.getCategory()).thenReturn(ZtpiCategory.FUTURE);
        when(userRepository.findAllById(anyList())).thenReturn(List.of(commenter));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            List<GroupReflectionCommentResponse> result = groupReflectionCommentService.findAll(groupId, reflectionId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).commenterId()).isEqualTo(authorId);
            assertThat(result.get(0).commenterNickname()).isEqualTo("작성자닉네임");
            assertThat(result.get(0).commenterCategory()).isEqualTo(ZtpiCategory.FUTURE);
            assertThat(result.get(0).content()).isEqualTo("댓글 내용");
        }
    }

    @Test
    void update_성공() {
        Long userId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;
        Long commentId = 500L;

        GroupMemberReflectionCommentEntity comment = mock(GroupMemberReflectionCommentEntity.class);
        when(comment.getUserId()).thenReturn(userId);
        when(groupMemberReflectionCommentRepository
                .findByIdAndGroupIdAndReflectionIdAndDeletedAtIsNull(commentId, groupId, reflectionId))
                .thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            groupReflectionCommentService.update(
                    groupId, reflectionId, commentId, new GroupReflectionCommentRequest("수정된 내용"));

            verify(comment).update("수정된 내용");
        }
    }

    @Test
    void update_존재하지_않으면_404() {
        Long userId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;
        Long commentId = 500L;

        when(groupMemberReflectionCommentRepository
                .findByIdAndGroupIdAndReflectionIdAndDeletedAtIsNull(commentId, groupId, reflectionId))
                .thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            assertThatThrownBy(() -> groupReflectionCommentService.update(
                    groupId, reflectionId, commentId, new GroupReflectionCommentRequest("내용")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_REFLECTION_COMMENT_NOT_FOUND));
        }
    }

    @Test
    void update_작성자가_아니면_403() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;
        Long commentId = 500L;

        GroupMemberReflectionCommentEntity comment = mock(GroupMemberReflectionCommentEntity.class);
        when(comment.getUserId()).thenReturn(authorId);
        when(groupMemberReflectionCommentRepository
                .findByIdAndGroupIdAndReflectionIdAndDeletedAtIsNull(commentId, groupId, reflectionId))
                .thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionCommentService.update(
                    groupId, reflectionId, commentId, new GroupReflectionCommentRequest("내용")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_REFLECTION_COMMENT_NOT_OWNER));
        }
    }

    @Test
    void delete_성공_소프트딜리트() {
        Long userId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;
        Long commentId = 500L;

        GroupMemberReflectionCommentEntity comment = mock(GroupMemberReflectionCommentEntity.class);
        when(comment.getUserId()).thenReturn(userId);
        when(groupMemberReflectionCommentRepository
                .findByIdAndGroupIdAndReflectionIdAndDeletedAtIsNull(commentId, groupId, reflectionId))
                .thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            groupReflectionCommentService.delete(groupId, reflectionId, commentId);

            verify(comment).softDelete();
        }
    }

    @Test
    void delete_작성자가_아니면_403() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;
        Long commentId = 500L;

        GroupMemberReflectionCommentEntity comment = mock(GroupMemberReflectionCommentEntity.class);
        when(comment.getUserId()).thenReturn(authorId);
        when(groupMemberReflectionCommentRepository
                .findByIdAndGroupIdAndReflectionIdAndDeletedAtIsNull(commentId, groupId, reflectionId))
                .thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupReflectionCommentService.delete(groupId, reflectionId, commentId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_REFLECTION_COMMENT_NOT_OWNER));
        }
    }
}
