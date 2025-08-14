package com.vpgh.dms.controller;

import com.vpgh.dms.model.UserGroupDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.service.UserGroupService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserGroupController {
    @Autowired
    private UserGroupService userGroupService;

    @PostMapping(path = "/secure/user-groups")
    @ApiMessage(message = "Tạo mới nhóm")
    public ResponseEntity<UserGroup> create(@RequestBody @Valid UserGroupDTO groupReq) {
        UserGroup group = this.userGroupService.handleCreateGroup(groupReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userGroupService.save(group));
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

}
