package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.RoleDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.CustomValidationException;
import com.vpgh.dms.util.exception.IdInvalidException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RoleController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private Validator validator;

    @PostMapping(path = "/secure/roles")
    @ApiMessage(message = "Tạo mới vai trò")
    public ResponseEntity<Role> create(@RequestBody @Valid RoleDTO reqRole) {
        Role role = new Role();
        role.setName(reqRole.getName());
        role.setDescription(reqRole.getDescription());
        role.setPermissions(new HashSet<>(reqRole.getPermissions()));
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.save(role));
    }

    @GetMapping(path = "/secure/roles")
    @ApiMessage(message = "Lấy danh sách vai trò")
    public ResponseEntity<PaginationResDTO<List<Role>>> list(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        Page<Role> pageRoles = this.roleService.getAllRoles(params);
        List<Role> roles = pageRoles.getContent();

        PaginationResDTO<List<Role>> results = new PaginationResDTO<>();
        results.setResult(roles);
        results.setCurrentPage(pageRoles.getNumber() + 1);
        results.setTotalPages(pageRoles.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping(path = "/secure/roles/{id}")
    @ApiMessage(message = "Lấy chi tiết vai trò")
    public ResponseEntity<Role> detail(@PathVariable(value = "id") Integer id) throws IdInvalidException {
        Role role = this.roleService.getRoleById(id);
        if (role == null) {
            throw new IdInvalidException("Không tìm thấy vai trò");
        }

        return ResponseEntity.status(HttpStatus.OK).body(role);
    }

    @PatchMapping(path = "/secure/roles/{id}")
    @ApiMessage(message = "Cập nhật vai trò")
    public ResponseEntity<?> update(@PathVariable("id") Integer id,
                                    @RequestBody RoleDTO reqRole) throws IdInvalidException {

        reqRole.setId(id);
        Set<ConstraintViolation<RoleDTO>> violations = validator.validate(reqRole);

        if (!violations.isEmpty()) {
            List<Map<String, String>> errorList = violations.stream().map(v -> {
                Map<String, String> err = new HashMap<>();
                err.put("field", v.getPropertyPath().toString());
                err.put("message", v.getMessage());
                return err;
            }).collect(Collectors.toList());
            throw new CustomValidationException(errorList);
        }

        Role role = this.roleService.getRoleById(id);
        if (role == null) {
            throw new IdInvalidException("Không tìm thấy vai trò");
        }

        role.setName(reqRole.getName());
        role.setDescription(reqRole.getDescription());
        role.setPermissions(new HashSet<>(reqRole.getPermissions()));

        return ResponseEntity.ok(this.roleService.save(role));
    }


    @DeleteMapping(path = "/secure/roles/{id}")
    @ApiMessage(message = "Xóa vai trò")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Integer id) throws IdInvalidException {
        Role role = this.roleService.getRoleById(id);
        if (role == null) {
            throw new IdInvalidException("Không tìm thấy vai trò");
        }

        this.roleService.deleteRoleById(role.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
