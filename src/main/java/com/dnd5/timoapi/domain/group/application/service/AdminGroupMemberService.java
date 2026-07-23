package com.dnd5.timoapi.domain.group.application.service;

import com.dnd5.timoapi.domain.group.domain.entity.GroupEntity;
import com.dnd5.timoapi.domain.group.domain.entity.GroupMemberEntity;
import com.dnd5.timoapi.domain.group.domain.repository.GroupMemberRepository;
import com.dnd5.timoapi.domain.group.domain.repository.GroupRepository;
import com.dnd5.timoapi.domain.group.exception.GroupErrorCode;
import com.dnd5.timoapi.domain.user.domain.entity.UserEntity;
import com.dnd5.timoapi.domain.user.domain.model.User;
import com.dnd5.timoapi.domain.user.domain.model.enums.OAuthProvider;
import com.dnd5.timoapi.domain.user.domain.repository.UserRepository;
import com.dnd5.timoapi.domain.user.exception.UserErrorCode;
import com.dnd5.timoapi.domain.user.presentation.response.UserResponse;
import com.dnd5.timoapi.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGroupMemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public List<UserResponse> getMembers(Long groupId) {
        GroupEntity entity = getGroupEntity(groupId);
        List<GroupMemberEntity> members = groupMemberRepository.findAllByGroupIdAndDeletedAtIsNull(entity.getId());

        return members.stream().map(
                e -> UserResponse.from(
                        buildUserModel(e.getUserId()), null
                )
        ).toList();
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        GroupMemberEntity entity = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_MEMBER_NOT_FOUND));

        entity.softDelete();
    }

    private User buildUserModel(Long id) {
        UserEntity entity = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return User.create(
                entity.getEmail(),
                entity.getNickname(),
                "Asia/Seoul",
                OAuthProvider.GOOGLE
        );
    }

    private GroupEntity getGroupEntity(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.GROUP_NOT_FOUND));
    }
}
