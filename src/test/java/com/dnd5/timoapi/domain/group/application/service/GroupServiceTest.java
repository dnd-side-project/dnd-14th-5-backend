package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupReflectionSort;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.request.GroupUpdateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupSummaryResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupTodayReflectionItem;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.test.domain.model.enums.ZtpiCategory;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ReflectionRepository reflectionRepository;

    @Mock
    private ReflectionQuestionRepository reflectionQuestionRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createGroup_FRIEND_성공_코드_자동_생성() {
        Long userId = 1L;
        GroupCreateRequest request = new GroupCreateRequest("팀A", GroupType.FRIEND, null, null);

        GroupEntity savedGroup = mock(GroupEntity.class);
        when(savedGroup.getId()).thenReturn(10L);
        when(savedGroup.toModel()).thenReturn(
                new com.dnd5.timoapi.domain.group.domain.model.Group(10L, "ABCD1234", "팀A", GroupType.FRIEND, null, null, null, null)
        );

        when(groupRepository.existsByCodeAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(groupRepository.save(any())).thenReturn(savedGroup);
        when(groupMemberRepository.save(any())).thenReturn(mock(GroupMemberEntity.class));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            GroupCreateResponse response = groupService.createGroup(request);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.code()).isEqualTo("ABCD1234");
        }

        verify(groupRepository).save(any());
        verify(groupMemberRepository).save(any());
    }

    @Test
    void createGroup_CHARACTER_category_없으면_400() {
        GroupCreateRequest request = new GroupCreateRequest("캐릭터그룹", GroupType.CHARACTER, null, null);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> groupService.createGroup(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_INVALID_CATEGORY));
        }
    }

    @Test
    void createGroup_FRIEND_category_있으면_400() {
        GroupCreateRequest request = new GroupCreateRequest("팀A", GroupType.FRIEND, null, ZtpiCategory.FUTURE);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> groupService.createGroup(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_INVALID_CATEGORY));
        }
    }

    @Test
    void getMyGroups_내_그룹만_반환() {
        Long userId = 1L;

        GroupMemberEntity membership = mock(GroupMemberEntity.class);
        when(membership.getGroupId()).thenReturn(10L);
        when(membership.getRole()).thenReturn(GroupMemberRole.OWNER);

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getId()).thenReturn(10L);
        when(groupEntity.toModel()).thenReturn(
                new com.dnd5.timoapi.domain.group.domain.model.Group(10L, "CODE1234", "팀A", GroupType.FRIEND, null, null, null, null)
        );

        when(groupMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId)).thenReturn(List.of(membership));
        when(groupRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.countByGroupIdAndDeletedAtIsNull(10L)).thenReturn(3L);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            List<GroupSummaryResponse> result = groupService.getMyGroups();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).memberCount()).isEqualTo(3);
            assertThat(result.get(0).myRole()).isEqualTo(GroupMemberRole.OWNER);
        }
    }

    @Test
    void getGroup_멤버가_code_없이_조회_성공() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.toModel()).thenReturn(
                new com.dnd5.timoapi.domain.group.domain.model.Group(groupId, "ABCD1234", "팀A", GroupType.FRIEND, null, null, null, null)
        );

        GroupMemberEntity member = mock(GroupMemberEntity.class);
        when(member.getRole()).thenReturn(GroupMemberRole.MEMBER);

        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(true);
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(Optional.of(member));
        when(groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(5L);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            GroupResponse response = groupService.getGroup(groupId, null);

            assertThat(response.isMember()).isTrue();
            assertThat(response.myRole()).isEqualTo(GroupMemberRole.MEMBER);
            assertThat(response.memberCount()).isEqualTo(5);
        }
    }

    @Test
    void getGroup_비멤버가_올바른_code로_조회_성공() {
        Long userId = 1L;
        Long groupId = 10L;
        String code = "ABCD1234";

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getCode()).thenReturn(code);
        when(groupEntity.toModel()).thenReturn(
                new com.dnd5.timoapi.domain.group.domain.model.Group(groupId, code, "팀A", GroupType.FRIEND, null, null, null, null)
        );

        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(false);
        when(groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(2L);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            GroupResponse response = groupService.getGroup(groupId, code);

            assertThat(response.isMember()).isFalse();
            assertThat(response.myRole()).isNull();
        }
    }

    @Test
    void getGroup_잘못된_code면_403() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getCode()).thenReturn("CORRECT1");
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            assertThatThrownBy(() -> groupService.getGroup(groupId, "WRONGCOD"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void getGroup_비멤버가_code_없이_조회시_403() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            assertThatThrownBy(() -> groupService.getGroup(groupId, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void updateGroup_OWNER가_아니면_403() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupMemberEntity member = mock(GroupMemberEntity.class);
        when(member.getRole()).thenReturn(GroupMemberRole.MEMBER);
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(Optional.of(member));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            assertThatThrownBy(() -> groupService.updateGroup(groupId, new GroupUpdateRequest("새이름", null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_FORBIDDEN));
        }
    }

    @Test
    void joinGroup_이미_참여중이면_409() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(true);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            assertThatThrownBy(() -> groupService.joinGroup(groupId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ALREADY_JOINED));
        }
    }

    @Test
    void joinGroup_탈퇴후_재가입시_restore() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        GroupMemberEntity softDeletedMember = mock(GroupMemberEntity.class);

        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(false);
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(softDeletedMember));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            groupService.joinGroup(groupId);

            verify(softDeletedMember).restore();
            verify(groupMemberRepository, never()).save(any());
        }
    }

    @Test
    void leaveGroup_OWNER_혼자면_그룹_소프트딜리트() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupMemberEntity ownerMember = mock(GroupMemberEntity.class);
        when(ownerMember.getRole()).thenReturn(GroupMemberRole.OWNER);
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(Optional.of(ownerMember));
        when(groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(1L);

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            groupService.leaveGroup(groupId);

            verify(groupEntity).softDelete();
            verify(ownerMember).softDelete();
        }
    }

    @Test
    void leaveGroup_OWNER_탈퇴시_다음_MEMBER에게_소유권_이전() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupMemberEntity ownerMember = mock(GroupMemberEntity.class);
        when(ownerMember.getRole()).thenReturn(GroupMemberRole.OWNER);
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(Optional.of(ownerMember));
        when(groupMemberRepository.countByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(3L);

        GroupMemberEntity nextMember = mock(GroupMemberEntity.class);
        when(groupMemberRepository.findTopByGroupIdAndRoleAndDeletedAtIsNullOrderByCreatedAtAsc(groupId, GroupMemberRole.MEMBER))
                .thenReturn(Optional.of(nextMember));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            groupService.leaveGroup(groupId);

            verify(nextMember).promoteToOwner();
            verify(ownerMember).softDelete();
        }
    }

    @Test
    void getTodayReflections_FRIEND_비멤버_403() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.FRIEND);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

        GroupMemberEntity member = mock(GroupMemberEntity.class);
        when(member.getUserId()).thenReturn(2L);
        when(groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(List.of(member));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            assertThatThrownBy(() -> groupService.getTodayReflections(groupId, GroupReflectionSort.LATEST))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void getTodayReflections_CHARACTER_전체_유저_회고_반환() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.CHARACTER);
        when(groupEntity.getCategory()).thenReturn(ZtpiCategory.FUTURE);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(2L);
        when(reflection.getQuestionId()).thenReturn(100L);
        when(reflection.getAnswerText()).thenReturn("회고 내용");

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.getId()).thenReturn(100L);
        when(question.getContent()).thenReturn("오늘의 질문");
        when(question.getCategory()).thenReturn(ZtpiCategory.FUTURE);

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(2L);
        when(user.getNickname()).thenReturn("홍길동");
        when(user.getStreakDays()).thenReturn(5);
        when(user.getTotalDays()).thenReturn(30);

        when(reflectionRepository.findAllByDateAndQuestionCategory(any(LocalDate.class), eq(ZtpiCategory.FUTURE)))
                .thenReturn(List.of(reflection));
        when(reflectionQuestionRepository.findAllById(anyList())).thenReturn(List.of(question));
        when(userRepository.findAllById(anyList())).thenReturn(List.of(user));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            List<GroupTodayReflectionItem> result = groupService.getTodayReflections(groupId, GroupReflectionSort.LATEST);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nickname()).isEqualTo("홍길동");
            assertThat(result.get(0).questionCategory()).isEqualTo(ZtpiCategory.FUTURE);
            assertThat(result.get(0).answerText()).isEqualTo("회고 내용");
            assertThat(result.get(0).streakDays()).isEqualTo(5);
            assertThat(result.get(0).totalDays()).isEqualTo(30);
        }
    }
}
