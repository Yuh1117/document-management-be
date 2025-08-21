package com.vpgh.dms.controller;

import com.vpgh.dms.model.UserGroupDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.model.entity.UserGroupMember;
import com.vpgh.dms.service.UserGroupService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.CustomValidationException;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import com.vpgh.dms.util.validator.GroupValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserGroupController {
    private final UserGroupService userGroupService;
    private final Validator validator;
    private final UserService userService;

    public UserGroupController(UserGroupService userGroupService, Validator validator, UserService userService) {
        this.userGroupService = userGroupService;
        this.validator = validator;
        this.userService = userService;
    }

    @PostMapping(path = "/secure/user-groups")
    @ApiMessage(message = "Tạo mới nhóm")
    public ResponseEntity<UserGroup> create(@RequestBody @Valid UserGroupDTO groupReq) {
        UserGroup group = this.userGroupService.handleCreateGroup(groupReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @GetMapping(path = "/secure/user-groups")
    @ApiMessage(message = "Danh sách nhóm")
    public ResponseEntity<PaginationResDTO<List<UserGroupDTO>>> list(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        Page<UserGroup> pageUsers = this.userGroupService.getAllGroupsByUser(params, SecurityUtil.getCurrentUserFromThreadLocal());
        List<UserGroupDTO> users = pageUsers.getContent().stream()
                .map(u -> this.userGroupService.convertUserGroupToUserGroupDTO(u)).collect(Collectors.toList());

        PaginationResDTO<List<UserGroupDTO>> results = new PaginationResDTO<>();
        results.setResult(users);
        results.setCurrentPage(pageUsers.getNumber() + 1);
        results.setTotalPages(pageUsers.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping(path = "/secure/user-groups/{id}")
    @ApiMessage(message = "Lấy chi tiết nhóm")
    public ResponseEntity<UserGroup> detail(@PathVariable(value = "id") Integer id) {
        UserGroup group = this.userGroupService.getGroupById(id);
        if (group == null) {
            throw new NotFoundException("Nhóm không tồn tại");
        }

        if (this.userGroupService.getMemberInGroup(group, SecurityUtil.getCurrentUserFromThreadLocal()) == null) {
            throw new ForbiddenException("Bạn không ở trong nhóm này.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(group);
    }

    @PatchMapping(path = "/secure/user-groups/{id}")
    @ApiMessage(message = "Cập nhật nhóm")
    public ResponseEntity<UserGroup> update(@PathVariable("id") Integer id,
                                            @RequestBody UserGroupDTO groupReq) {

        UserGroup group = this.userGroupService.getGroupById(id);
        if (group == null) {
            throw new NotFoundException("Nhóm không tồn tại");
        }

        groupReq.setId(group.getId());
        GroupValidator.setCurrentEntity(group);
        Set<ConstraintViolation<UserGroupDTO>> violations = validator.validate(groupReq);

        if (!violations.isEmpty()) {
            List<Map<String, String>> errorList = violations.stream().map(v -> {
                Map<String, String> err = new HashMap<>();
                err.put("field", v.getPropertyPath().toString());
                err.put("message", v.getMessage());
                return err;
            }).collect(Collectors.toList());
            throw new CustomValidationException(errorList);
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        UserGroupMember member = this.userGroupService.getMemberInGroup(group, currentUser);
        if (member == null) {
            throw new ForbiddenException("Bạn không ở trong nhóm này.");
        }

        if (!this.userGroupService.isAdminGroup(member)) {
            throw new ForbiddenException("Bạn không có quyền thực hiện.");
        }

        if (groupReq.getMembers() != null) {
            boolean check = groupReq.getMembers().stream().anyMatch(m ->
                    this.userGroupService.isOwnerGroup(group, this.userService.getUserByEmail(m.getEmail())));
            if (check) {
                throw new ForbiddenException("Không thể cập nhật chủ nhóm.");
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.userGroupService.handleUpdateGroup(group, groupReq));
    }

    @PatchMapping(path = "/secure/user-groups/{id}/quit")
    @ApiMessage(message = "Thoát khỏi nhóm")
    public ResponseEntity<Void> quitGroup(@PathVariable("id") Integer id) {
        UserGroup group = this.userGroupService.getGroupById(id);
        if (group == null) {
            throw new NotFoundException("Nhóm không tồn tại");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        UserGroupMember member = this.userGroupService.getMemberInGroup(group, currentUser);
        if (member == null) {
            throw new ForbiddenException("Bạn không ở trong nhóm này.");
        }

        if (this.userGroupService.isOwnerGroup(group, currentUser)) {
            throw new ForbiddenException("Bạn không thể rời khỏi nhóm.");
        }

        group.getMembers().remove(member);
        this.userGroupService.save(group);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping(path = "/secure/user-groups/{id}")
    @ApiMessage(message = "Xóa nhóm")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Integer id) {
        UserGroup group = this.userGroupService.getGroupById(id);
        if (group == null) {
            throw new NotFoundException("Nhóm không tồn tại");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        UserGroupMember member = this.userGroupService.getMemberInGroup(group, currentUser);
        if (member == null) {
            throw new ForbiddenException("Bạn không ở trong nhóm này.");
        }

        if (!this.userGroupService.isOwnerGroup(group, currentUser)) {
            throw new ForbiddenException("Bạn không thể xoá nhóm");
        }

        this.userGroupService.deleteGroupById(group.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
