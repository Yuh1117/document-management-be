package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.UserGroupDTO;
import com.vpgh.dms.model.constant.MemberEnum;
import com.vpgh.dms.model.dto.MemberDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.model.entity.UserGroupMember;
import com.vpgh.dms.repository.UserGroupMemberRepository;
import com.vpgh.dms.repository.UserGroupRepository;
import com.vpgh.dms.repository.UserRepository;
import com.vpgh.dms.service.UserGroupService;
import com.vpgh.dms.service.specification.UserGroupSpecification;
import com.vpgh.dms.util.PageSize;
import com.vpgh.dms.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupMemberRepository userGroupMemberRepository;
    private final UserRepository userRepository;

    public UserGroupServiceImpl(UserGroupRepository userGroupRepository, UserGroupMemberRepository userGroupMemberRepository,
                                UserRepository userRepository) {
        this.userGroupRepository = userGroupRepository;
        this.userGroupMemberRepository = userGroupMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserGroup save(UserGroup group) {
        return this.userGroupRepository.save(group);
    }

    @Override
    public UserGroup getGroupById(Integer id) {
        return this.userGroupRepository.findById(id).orElse(null);
    }

    @Override
    public UserGroup handleCreateGroup(UserGroupDTO dto) {
        UserGroup group = new UserGroup();
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());

        UserGroupMember creatorMember = new UserGroupMember();
        creatorMember.setUser(SecurityUtil.getCurrentUserFromThreadLocal());
        creatorMember.setGroup(group);
        creatorMember.setRole(MemberEnum.ADMIN);

        group.setMembers(new HashSet<>(Set.of(creatorMember)));
        return save(group);
    }

    @Override
    public UserGroup handleUpdateGroup(UserGroup group, UserGroupDTO dto) {
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());

        if (dto.getMembers() != null) {
            Map<Integer, UserGroupMember> existingMembersMap = group.getMembers().stream()
                    .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m));

            Map<Integer, MemberDTO> requestedMembersMap = dto.getMembers().stream()
                    .map(m -> {
                        User user = this.userRepository.findByEmail(m.getEmail());
                        m.setId(user.getId());
                        return m;
                    })
                    .collect(Collectors.toMap(MemberDTO::getId, m -> m, (m1, m2) -> m1));

            for (Map.Entry<Integer, MemberDTO> entry : requestedMembersMap.entrySet()) {
                Integer id = entry.getKey();
                MemberDTO memberDTO = entry.getValue();
                User user = this.userRepository.findById(id).orElse(null);

                if (!existingMembersMap.containsKey(id)) {
                    UserGroupMember newMember = new UserGroupMember();
                    newMember.setGroup(group);
                    newMember.setUser(user);
                    newMember.setRole(memberDTO.getRole() != null ? memberDTO.getRole() : MemberEnum.MEMBER);
                    group.getMembers().add(newMember);
                } else {
                    UserGroupMember existingMember = existingMembersMap.get(id);
                    existingMember.setRole(memberDTO.getRole() != null ? memberDTO.getRole() : MemberEnum.MEMBER);
                }
            }

            group.getMembers().removeIf(m -> {
                Integer memberId = m.getUser().getId();
                return !requestedMembersMap.containsKey(memberId) && !memberId.equals(group.getCreatedBy().getId());
            });
        }
        return save(group);
    }

    @Override
    public List<UserGroup> getGroupsByUser(User user) {
        return this.userGroupMemberRepository.findByUser(user)
                .stream().map(m -> m.getGroup()).collect(Collectors.toList());
    }

    @Override
    public boolean existsByNameAndCreatedByAndIdNot(String name, User createdBy, Integer id) {
        return this.userGroupRepository.existsByNameAndCreatedByAndIdNot(name, createdBy, id);
    }

    @Override
    public Page<UserGroup> getAllGroups(Map<String, String> params) {
        Specification<UserGroup> combinedSpec = Specification.allOf();
        Pageable pageable = Pageable.unpaged();

        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            String kw = params.get("kw");

            pageable = PageRequest.of(page - 1, PageSize.GROUP_PAGE_SIZE.getSize(),
                    Sort.by(Sort.Order.asc("id")));
            if (kw != null && !kw.isEmpty()) {
                Specification<UserGroup> spec = UserGroupSpecification.filterByKeyword(params.get("kw"));
                combinedSpec = combinedSpec.and(spec);
            }
        }

        return this.userGroupRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public Page<UserGroup> getAllGroupsByUser(Map<String, String> params, User user) {
        Specification<UserGroup> combinedSpec = Specification.allOf();
        Pageable pageable = Pageable.unpaged();

        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            String kw = params.get("kw");

            pageable = PageRequest.of(page - 1, PageSize.GROUP_PAGE_SIZE.getSize(),
                    Sort.by(Sort.Order.asc("id")));
            if (kw != null && !kw.isEmpty()) {
                Specification<UserGroup> spec = UserGroupSpecification.filterByKeyword(params.get("kw"));
                combinedSpec = combinedSpec.and(spec);
            }
        }

        combinedSpec = combinedSpec.and(UserGroupSpecification.hasCreatedBy(user));
        return this.userGroupRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public UserGroupDTO convertUserGroupToUserGroupDTO(UserGroup group) {
        UserGroupDTO dto = new UserGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        return dto;
    }

    @Override
    public void deleteGroupById(Integer id) {
        this.userGroupRepository.deleteById(id);
    }

    @Override
    public UserGroupMember getMemberInGroup(UserGroup group, User user) {
        Optional<UserGroupMember> member = group.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findFirst();
        return member.orElse(null);
    }

    @Override
    public boolean isOwnerGroup(UserGroup group, User user) {
        return group.getCreatedBy().getId().equals(user.getId());
    }

    @Override
    public boolean isAdminGroup(UserGroupMember member) {
        return member.getRole().equals(MemberEnum.ADMIN);
    }
}
