package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupReflectionSort;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionCommentRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionLikeRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberReflectionPrivateRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.request.GroupUpdateRequest;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberReflectionPrivateEntity;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupDetailResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupMemberReflectionDetailResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupMemberReflectionResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupTodayReflectionItem;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionEntity;
import com.dnd5.timoapi.domain.reflection.domain.entity.ReflectionQuestionEntity;
import com.dnd5.timoapi.domain.reflection.domain.model.ReflectionQuestion;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionQuestionRepository;
import com.dnd5.timoapi.domain.reflection.domain.repository.ReflectionRepository;
import com.dnd5.timoapi.domain.reflection.exception.ReflectionErrorCode;
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
import java.time.LocalDateTime;
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

    @Mock
    private GroupMemberReflectionPrivateRepository groupMemberReflectionPrivateRepository;

    @Mock
    private GroupMemberReflectionLikeRepository groupMemberReflectionLikeRepository;

    @Mock
    private GroupMemberReflectionCommentRepository groupMemberReflectionCommentRepository;

    @Test
    void createGroup_FRIEND_성공_코드_자동_생성() {
        Long userId = 1L;
        GroupCreateRequest request = new GroupCreateRequest("팀A", GroupType.FRIEND, null);

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
    void createGroup_CHARACTER_타입이면_400() {
        GroupCreateRequest request = new GroupCreateRequest("캐릭터그룹", GroupType.CHARACTER, null);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> groupService.createGroup(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_INVALID_CATEGORY));
        }
    }

    @Test
    void getMyGroups_내_FRIEND_그룹_반환() {
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
        when(groupRepository.findAllByTypeAndDeletedAtIsNull(GroupType.CHARACTER)).thenReturn(List.of());

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            List<GroupResponse> result = groupService.getMyGroups();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).memberCount()).isEqualTo(3);
            assertThat(result.get(0).myRole()).isEqualTo(GroupMemberRole.OWNER);
        }
    }

    @Test
    void getMyGroups_캐릭터그룹은_미멤버여도_포함() {
        Long userId = 1L;

        GroupEntity characterGroup = mock(GroupEntity.class);
        when(characterGroup.getId()).thenReturn(20L);
        when(characterGroup.toModel()).thenReturn(
                new com.dnd5.timoapi.domain.group.domain.model.Group(20L, "CHAR1234", "그늘이", GroupType.CHARACTER, null, ZtpiCategory.PAST_NEGATIVE, null, null)
        );

        when(groupMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId)).thenReturn(List.of());
        when(groupRepository.findAllByTypeAndDeletedAtIsNull(GroupType.CHARACTER)).thenReturn(List.of(characterGroup));
        when(groupMemberRepository.countByGroupIdAndDeletedAtIsNull(20L)).thenReturn(0L);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            List<GroupResponse> result = groupService.getMyGroups();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).type()).isEqualTo(GroupType.CHARACTER);
            assertThat(result.get(0).myRole()).isNull();
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

            GroupDetailResponse response = groupService.getGroup(groupId, null);

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

            GroupDetailResponse response = groupService.getGroup(groupId, code);

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

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.FRIEND);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

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
    void joinGroupByCode_이미_참여중이면_409() {
        Long userId = 1L;
        String code = "ABCD1234";
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.FRIEND);
        when(groupEntity.getId()).thenReturn(groupId);
        when(groupRepository.findByCodeAndDeletedAtIsNull(code)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(true);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            assertThatThrownBy(() -> groupService.joinGroupByCode(code))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ALREADY_JOINED));
        }
    }

    @Test
    void joinGroupByCode_탈퇴후_재가입시_restore() {
        Long userId = 1L;
        String code = "ABCD1234";
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.FRIEND);
        when(groupEntity.getId()).thenReturn(groupId);
        GroupMemberEntity softDeletedMember = mock(GroupMemberEntity.class);

        when(groupRepository.findByCodeAndDeletedAtIsNull(code)).thenReturn(Optional.of(groupEntity));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(false);
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(softDeletedMember));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            groupService.joinGroupByCode(code);

            verify(softDeletedMember).restoreAsMember();
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
    void getTodayReflections_FRIEND_회고_안한_멤버도_포함() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.FRIEND);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

        GroupMemberEntity member1 = mock(GroupMemberEntity.class);
        when(member1.getUserId()).thenReturn(1L);
        GroupMemberEntity member2 = mock(GroupMemberEntity.class);
        when(member2.getUserId()).thenReturn(2L);
        when(groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(List.of(member1, member2));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(1L);
        when(reflection.getQuestionId()).thenReturn(100L);
        when(reflection.getAnswerText()).thenReturn("회고 내용");
        when(reflectionRepository.findAllByDateAndUserIdIn(any(LocalDate.class), anyList()))
                .thenReturn(List.of(reflection));

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.getId()).thenReturn(100L);
        when(question.getContent()).thenReturn("오늘의 질문");
        when(question.getCategory()).thenReturn(ZtpiCategory.FUTURE);
        when(reflectionQuestionRepository.findAllById(anyList())).thenReturn(List.of(question));

        UserEntity user1 = mock(UserEntity.class);
        when(user1.getId()).thenReturn(1L);
        when(user1.getNickname()).thenReturn("유저1");
        when(user1.getStreakDays()).thenReturn(5);
        when(user1.getTotalDays()).thenReturn(10);

        UserEntity user2 = mock(UserEntity.class);
        when(user2.getId()).thenReturn(2L);
        when(user2.getNickname()).thenReturn("유저2");
        when(user2.getStreakDays()).thenReturn(3);
        when(user2.getTotalDays()).thenReturn(7);

        when(userRepository.findAllById(anyList())).thenReturn(List.of(user1, user2));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            List<GroupTodayReflectionItem> result = groupService.getTodayReflections(groupId, GroupReflectionSort.LATEST);

            assertThat(result).hasSize(2);
            GroupTodayReflectionItem withReflection = result.stream().filter(r -> r.userId().equals(1L)).findFirst().orElseThrow();
            GroupTodayReflectionItem withoutReflection = result.stream().filter(r -> r.userId().equals(2L)).findFirst().orElseThrow();
            assertThat(withReflection.answerText()).isEqualTo("회고 내용");
            assertThat(withoutReflection.answerText()).isNull();
            assertThat(withoutReflection.questionContent()).isNull();
        }
    }

    @Test
    void getTodayReflections_FRIEND_비멤버_403() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.FRIEND);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

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
    void getTodayReflections_CHARACTER_멤버만_회고_반환() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getId()).thenReturn(groupId);
        when(groupEntity.getType()).thenReturn(GroupType.CHARACTER);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

        GroupMemberEntity member = mock(GroupMemberEntity.class);
        when(member.getUserId()).thenReturn(2L);
        when(groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(List.of(member));

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

        when(reflectionRepository.findAllByDateAndUserIdIn(any(LocalDate.class), anyList()))
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

    @Test
    void getTodayReflections_CHARACTER_회고_안한_멤버도_포함() {
        Long userId = 1L;
        Long groupId = 10L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getId()).thenReturn(groupId);
        when(groupEntity.getType()).thenReturn(GroupType.CHARACTER);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

        GroupMemberEntity member1 = mock(GroupMemberEntity.class);
        when(member1.getUserId()).thenReturn(2L);
        GroupMemberEntity member2 = mock(GroupMemberEntity.class);
        when(member2.getUserId()).thenReturn(3L);
        when(groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(List.of(member1, member2));

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getUserId()).thenReturn(2L);
        when(reflection.getQuestionId()).thenReturn(100L);
        when(reflection.getAnswerText()).thenReturn("회고 내용");

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.getId()).thenReturn(100L);
        when(question.getContent()).thenReturn("오늘의 질문");
        when(question.getCategory()).thenReturn(ZtpiCategory.FUTURE);

        UserEntity user1 = mock(UserEntity.class);
        when(user1.getId()).thenReturn(2L);
        when(user1.getNickname()).thenReturn("홍길동");
        when(user1.getStreakDays()).thenReturn(5);
        when(user1.getTotalDays()).thenReturn(30);

        UserEntity user2 = mock(UserEntity.class);
        when(user2.getId()).thenReturn(3L);
        when(user2.getNickname()).thenReturn("김철수");
        when(user2.getStreakDays()).thenReturn(2);
        when(user2.getTotalDays()).thenReturn(10);

        when(reflectionRepository.findAllByDateAndUserIdIn(any(LocalDate.class), anyList()))
                .thenReturn(List.of(reflection));
        when(reflectionQuestionRepository.findAllById(anyList())).thenReturn(List.of(question));
        when(userRepository.findAllById(anyList())).thenReturn(List.of(user1, user2));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            List<GroupTodayReflectionItem> result = groupService.getTodayReflections(groupId, GroupReflectionSort.LATEST);

            assertThat(result).hasSize(2);
            GroupTodayReflectionItem withReflection = result.stream().filter(r -> r.userId().equals(2L)).findFirst().orElseThrow();
            GroupTodayReflectionItem withoutReflection = result.stream().filter(r -> r.userId().equals(3L)).findFirst().orElseThrow();
            assertThat(withReflection.answerText()).isEqualTo("회고 내용");
            assertThat(withoutReflection.answerText()).isNull();
            assertThat(withoutReflection.questionContent()).isNull();
        }
    }

    @Test
    void getTodayReflections_FRIEND_비공개_회고는_타인에게_content_null() {
        Long viewerId = 99L;
        Long groupId = 10L;
        Long authorId = 1L;
        Long reflectionId = 500L;

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getType()).thenReturn(GroupType.FRIEND);
        when(groupRepository.findByIdAndDeletedAtIsNull(groupId)).thenReturn(Optional.of(groupEntity));

        GroupMemberEntity member = mock(GroupMemberEntity.class);
        when(member.getUserId()).thenReturn(authorId);
        when(groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(groupId)).thenReturn(List.of(member));
        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getId()).thenReturn(reflectionId);
        when(reflection.getUserId()).thenReturn(authorId);
        when(reflection.getQuestionId()).thenReturn(100L);
        when(reflectionRepository.findAllByDateAndUserIdIn(any(LocalDate.class), anyList()))
                .thenReturn(List.of(reflection));

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.getId()).thenReturn(100L);
        when(question.getContent()).thenReturn("오늘의 질문");
        when(question.getCategory()).thenReturn(ZtpiCategory.FUTURE);
        when(reflectionQuestionRepository.findAllById(anyList())).thenReturn(List.of(question));

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(authorId);
        when(user.getNickname()).thenReturn("작성자");
        when(user.getStreakDays()).thenReturn(1);
        when(user.getTotalDays()).thenReturn(1);
        when(userRepository.findAllById(anyList())).thenReturn(List.of(user));

        GroupMemberReflectionPrivateEntity privateEntity = mock(GroupMemberReflectionPrivateEntity.class);
        when(privateEntity.getReflectionId()).thenReturn(reflectionId);
        when(groupMemberReflectionPrivateRepository.findAllByGroupIdAndReflectionIdIn(eq(groupId), anyList()))
                .thenReturn(List.of(privateEntity));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            List<GroupTodayReflectionItem> result = groupService.getTodayReflections(groupId, GroupReflectionSort.LATEST);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).reflectionId()).isEqualTo(reflectionId);
            assertThat(result.get(0).isPublic()).isFalse();
            assertThat(result.get(0).answerText()).isNull();
        }
    }

    @Test
    void getMemberReflection_공개_회고는_타인도_content_조회_가능() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getId()).thenReturn(reflectionId);
        when(reflection.getUserId()).thenReturn(authorId);
        when(reflection.getQuestionId()).thenReturn(200L);
        when(reflection.getAnswerText()).thenReturn("공개 회고 내용");
        when(reflection.getDate()).thenReturn(LocalDate.of(2026, 8, 1));
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflection));

        GroupMemberEntity authorMember = mock(GroupMemberEntity.class);
        when(authorMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, authorId))
                .thenReturn(Optional.of(authorMember));

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.toModel()).thenReturn(
                new ReflectionQuestion(200L, 1L, ZtpiCategory.FUTURE, "질문", "admin", null, null));
        when(reflectionQuestionRepository.findById(200L)).thenReturn(Optional.of(question));

        when(groupMemberReflectionPrivateRepository.existsByGroupIdAndReflectionId(groupId, reflectionId)).thenReturn(false);
        when(groupMemberReflectionLikeRepository.countByGroupIdAndReflectionId(groupId, reflectionId)).thenReturn(3L);
        when(groupMemberReflectionCommentRepository.countByGroupIdAndReflectionIdAndDeletedAtIsNull(groupId, reflectionId))
                .thenReturn(2L);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            GroupMemberReflectionDetailResponse response = groupService.getMemberReflection(groupId, reflectionId);

            assertThat(response.content()).isEqualTo("공개 회고 내용");
            assertThat(response.likes()).isEqualTo(3L);
            assertThat(response.comments()).isEqualTo(2L);
        }
    }

    @Test
    void getMemberReflection_비공개_회고는_타인에게_content_null() {
        Long viewerId = 1L;
        Long authorId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getId()).thenReturn(reflectionId);
        when(reflection.getUserId()).thenReturn(authorId);
        when(reflection.getQuestionId()).thenReturn(200L);
        when(reflection.getDate()).thenReturn(LocalDate.of(2026, 8, 1));
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflection));

        GroupMemberEntity authorMember = mock(GroupMemberEntity.class);
        when(authorMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, authorId))
                .thenReturn(Optional.of(authorMember));

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.toModel()).thenReturn(
                new ReflectionQuestion(200L, 1L, ZtpiCategory.FUTURE, "질문", "admin", null, null));
        when(reflectionQuestionRepository.findById(200L)).thenReturn(Optional.of(question));

        when(groupMemberReflectionPrivateRepository.existsByGroupIdAndReflectionId(groupId, reflectionId)).thenReturn(true);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            GroupMemberReflectionDetailResponse response = groupService.getMemberReflection(groupId, reflectionId);

            assertThat(response.content()).isNull();
        }
    }

    @Test
    void getMemberReflection_본인_비공개_회고는_content_노출() {
        Long userId = 2L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(true);

        ReflectionEntity reflection = mock(ReflectionEntity.class);
        when(reflection.getId()).thenReturn(reflectionId);
        when(reflection.getUserId()).thenReturn(userId);
        when(reflection.getQuestionId()).thenReturn(200L);
        when(reflection.getAnswerText()).thenReturn("내 비공개 회고");
        when(reflection.getDate()).thenReturn(LocalDate.of(2026, 8, 1));
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.of(reflection));

        GroupMemberEntity authorMember = mock(GroupMemberEntity.class);
        when(authorMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId))
                .thenReturn(Optional.of(authorMember));

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.toModel()).thenReturn(
                new ReflectionQuestion(200L, 1L, ZtpiCategory.FUTURE, "질문", "admin", null, null));
        when(reflectionQuestionRepository.findById(200L)).thenReturn(Optional.of(question));

        when(groupMemberReflectionPrivateRepository.existsByGroupIdAndReflectionId(groupId, reflectionId)).thenReturn(true);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            GroupMemberReflectionDetailResponse response = groupService.getMemberReflection(groupId, reflectionId);

            assertThat(response.content()).isEqualTo("내 비공개 회고");
        }
    }

    @Test
    void getMemberReflection_비멤버_조회시_403() {
        Long viewerId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupService.getMemberReflection(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }

    @Test
    void getMemberReflection_존재하지_않는_회고면_404() {
        Long viewerId = 1L;
        Long groupId = 10L;
        Long reflectionId = 100L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);
        when(reflectionRepository.findById(reflectionId)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupService.getMemberReflection(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ReflectionErrorCode.REFLECTION_NOT_FOUND));
        }
    }

    @Test
    void getMemberReflection_작성자_그룹_가입일_이전_회고면_404() {
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

            assertThatThrownBy(() -> groupService.getMemberReflection(groupId, reflectionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ReflectionErrorCode.REFLECTION_NOT_FOUND));
        }
    }

    @Test
    void getMemberCalendar_가입일_이후_회고만_비공개_마스킹해_반환() {
        Long viewerId = 1L;
        Long targetUserId = 2L;
        Long groupId = 10L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);

        GroupMemberEntity targetMember = mock(GroupMemberEntity.class);
        when(targetMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, targetUserId))
                .thenReturn(Optional.of(targetMember));

        ReflectionEntity publicReflection = mock(ReflectionEntity.class);
        when(publicReflection.getId()).thenReturn(1L);
        when(publicReflection.getQuestionId()).thenReturn(200L);
        when(publicReflection.getAnswerText()).thenReturn("공개 회고");
        when(publicReflection.getDate()).thenReturn(LocalDate.of(2026, 7, 5));

        ReflectionEntity privateReflection = mock(ReflectionEntity.class);
        when(privateReflection.getId()).thenReturn(2L);
        when(privateReflection.getQuestionId()).thenReturn(200L);
        when(privateReflection.getDate()).thenReturn(LocalDate.of(2026, 7, 6));

        when(reflectionRepository.findAllByUserIdAndDateBetween(eq(targetUserId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(publicReflection, privateReflection));

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.getId()).thenReturn(200L);
        when(question.toModel()).thenReturn(
                new ReflectionQuestion(200L, 1L, ZtpiCategory.FUTURE, "질문", "admin", null, null));
        when(reflectionQuestionRepository.findAllById(anyList())).thenReturn(List.of(question));

        GroupMemberReflectionPrivateEntity privateEntity = mock(GroupMemberReflectionPrivateEntity.class);
        when(privateEntity.getReflectionId()).thenReturn(2L);
        when(groupMemberReflectionPrivateRepository.findAllByGroupIdAndReflectionIdIn(eq(groupId), anyList()))
                .thenReturn(List.of(privateEntity));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            List<GroupMemberReflectionResponse> result = groupService.getMemberCalendar(groupId, targetUserId);

            assertThat(result).hasSize(2);
            GroupMemberReflectionResponse publicItem = result.stream().filter(r -> r.id().equals(1L)).findFirst().orElseThrow();
            GroupMemberReflectionResponse privateItem = result.stream().filter(r -> r.id().equals(2L)).findFirst().orElseThrow();
            assertThat(publicItem.isPublic()).isTrue();
            assertThat(publicItem.content()).isEqualTo("공개 회고");
            assertThat(privateItem.isPublic()).isFalse();
            assertThat(privateItem.content()).isNull();
        }
    }

    @Test
    void getMemberCalendar_본인_캘린더면_비공개도_content_노출() {
        Long userId = 2L;
        Long groupId = 10L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId)).thenReturn(true);

        GroupMemberEntity targetMember = mock(GroupMemberEntity.class);
        when(targetMember.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, userId))
                .thenReturn(Optional.of(targetMember));

        ReflectionEntity privateReflection = mock(ReflectionEntity.class);
        when(privateReflection.getId()).thenReturn(2L);
        when(privateReflection.getQuestionId()).thenReturn(200L);
        when(privateReflection.getAnswerText()).thenReturn("내 비공개 회고");
        when(privateReflection.getDate()).thenReturn(LocalDate.of(2026, 7, 6));

        when(reflectionRepository.findAllByUserIdAndDateBetween(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(privateReflection));

        ReflectionQuestionEntity question = mock(ReflectionQuestionEntity.class);
        when(question.getId()).thenReturn(200L);
        when(question.toModel()).thenReturn(
                new ReflectionQuestion(200L, 1L, ZtpiCategory.FUTURE, "질문", "admin", null, null));
        when(reflectionQuestionRepository.findAllById(anyList())).thenReturn(List.of(question));

        GroupMemberReflectionPrivateEntity privateEntity = mock(GroupMemberReflectionPrivateEntity.class);
        when(privateEntity.getReflectionId()).thenReturn(2L);
        when(groupMemberReflectionPrivateRepository.findAllByGroupIdAndReflectionIdIn(eq(groupId), anyList()))
                .thenReturn(List.of(privateEntity));

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            List<GroupMemberReflectionResponse> result = groupService.getMemberCalendar(groupId, userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isPublic()).isFalse();
            assertThat(result.get(0).content()).isEqualTo("내 비공개 회고");
        }
    }

    @Test
    void getMemberCalendar_대상_유저가_그룹_멤버가_아니면_404() {
        Long viewerId = 1L;
        Long targetUserId = 2L;
        Long groupId = 10L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(true);
        when(groupMemberRepository.findByGroupIdAndUserIdAndDeletedAtIsNull(groupId, targetUserId))
                .thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupService.getMemberCalendar(groupId, targetUserId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_MEMBER_NOT_FOUND));
        }
    }

    @Test
    void getMemberCalendar_뷰어가_비멤버면_403() {
        Long viewerId = 1L;
        Long targetUserId = 2L;
        Long groupId = 10L;

        when(groupMemberRepository.existsByGroupIdAndUserIdAndDeletedAtIsNull(groupId, viewerId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(viewerId);

            assertThatThrownBy(() -> groupService.getMemberCalendar(groupId, targetUserId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(GroupErrorCode.GROUP_ACCESS_DENIED));
        }
    }
}
