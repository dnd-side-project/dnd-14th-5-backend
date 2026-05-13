package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupMemberRole;
import com.dnd5.timoapi.domain.group.domain.model.enums.GroupType;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.group.presentation.request.GroupCreateRequest;
import com.dnd5.timoapi.domain.group.presentation.request.GroupUpdateRequest;
import com.dnd5.timoapi.domain.group.presentation.response.GroupCreateResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupResponse;
import com.dnd5.timoapi.domain.group.presentation.response.GroupSummaryResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import com.dnd5.timoapi.global.security.context.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void createGroup_성공_코드_자동_생성() {
        Long userId = 1L;
        GroupCreateRequest request = new GroupCreateRequest("팀A", GroupType.FRIEND, null);

        GroupEntity savedGroup = mock(GroupEntity.class);
        when(savedGroup.getId()).thenReturn(10L);
        when(savedGroup.toModel()).thenReturn(
                new com.dnd5.timoapi.domain.group.domain.model.Group(10L, "ABCD1234", "팀A", GroupType.FRIEND, null, null, null)
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
    void getMyGroups_내_그룹만_반환() {
        Long userId = 1L;

        GroupMemberEntity membership = mock(GroupMemberEntity.class);
        when(membership.getGroupId()).thenReturn(10L);
        when(membership.getRole()).thenReturn(GroupMemberRole.OWNER);

        GroupEntity groupEntity = mock(GroupEntity.class);
        when(groupEntity.getId()).thenReturn(10L);
        when(groupEntity.toModel()).thenReturn(
                new com.dnd5.timoapi.domain.group.domain.model.Group(10L, "CODE1234", "팀A", GroupType.FRIEND, null, null, null)
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
                new com.dnd5.timoapi.domain.group.domain.model.Group(groupId, "ABCD1234", "팀A", GroupType.FRIEND, null, null, null)
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
                new com.dnd5.timoapi.domain.group.domain.model.Group(groupId, code, "팀A", GroupType.FRIEND, null, null, null)
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
}
