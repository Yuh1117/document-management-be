package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.UserGroupDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.repository.UserGroupMemberRepository;
import com.vpgh.dms.repository.UserGroupRepository;
import com.vpgh.dms.service.UserGroupService;
import com.vpgh.dms.service.specification.UserGroupSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserGroupServiceImpl implements UserGroupService {

    @Autowired
    private UserGroupRepository userGroupRepository;
    @Autowired
    private UserGroupMemberRepository userGroupMemberRepository;

    @Override
    public UserGroup save(UserGroup group) {
        return this.userGroupRepository.save(group);
    }

    @Override
    public UserGroup handleCreateGroup(UserGroupDTO dto) {
        UserGroup group = new UserGroup();
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
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
}
