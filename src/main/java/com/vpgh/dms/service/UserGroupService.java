package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.UserGroupDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.model.entity.UserGroupMember;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface UserGroupService {
    UserGroup save(UserGroup group);

    UserGroup getGroupById(Integer id);

    UserGroup handleCreateGroup(UserGroupDTO dto);

    UserGroup handleUpdateGroup(UserGroup group, UserGroupDTO dto);

    List<UserGroup> getGroupsByUser(User user);

    boolean existsByNameAndCreatedByAndIdNot(String name, User createdBy, Integer id);

    Page<UserGroup> getAllGroups(Map<String, String> params);

    Page<UserGroup> getAllGroupsByUser(Map<String, String> params, User user);

    UserGroupDTO convertUserGroupToUserGroupDTO(UserGroup group);

    void deleteGroupById(Integer id);

    UserGroupMember getMemberInGroup(UserGroup group, User user);

    boolean isAdminGroup(UserGroupMember member);

    boolean isOwnerGroup(UserGroup group, User user);
}
