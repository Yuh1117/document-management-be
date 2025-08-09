package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.PermissionDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.CustomValidationException;
import com.vpgh.dms.util.exception.DataConflictException;
import com.vpgh.dms.util.exception.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private Validator validator;

    @PostMapping(path = "/secure/permissions")
    @ApiMessage(message = "Tạo mới quyền")
    public ResponseEntity<Permission> create(@RequestBody @Valid PermissionDTO reqPermission) {
        Permission permission = this.permissionService.handleCreatePermission(reqPermission);
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @GetMapping(path = "/secure/permissions")
    @ApiMessage(message = "Lấy danh sách quyền")
    public ResponseEntity<PaginationResDTO<List<Permission>>> list(@RequestParam Map<String, String> params) {
        if (params.get("all") != null && "true".equalsIgnoreCase(params.get("all"))) {
            params = null;
        } else {
            String page = params.get("page");
            if (page == null || page.isEmpty()) {
                params.put("page", "1");
            }
        }

        Page<Permission> pagePermissions = this.permissionService.getAllPermission(params);
        List<Permission> permissions = pagePermissions.getContent();

        PaginationResDTO<List<Permission>> results = new PaginationResDTO<>();
        results.setResult(permissions);
        results.setCurrentPage(pagePermissions.getNumber() + 1);
        results.setTotalPages(pagePermissions.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping(path = "/secure/permissions/{id}")
    @ApiMessage(message = "Lấy chi tiết quyền")
    public ResponseEntity<Permission> detail(@PathVariable(value = "id") Integer id) {
        Permission permission = this.permissionService.getPermissionById(id);
        if (permission == null) {
            throw new NotFoundException("Không tìm thấy quyền");
        }

        return ResponseEntity.status(HttpStatus.OK).body(permission);
    }

    @PatchMapping(path = "/secure/permissions/{id}")
    @ApiMessage(message = "Cập nhật quyền")
    public ResponseEntity<Permission> update(@PathVariable("id") Integer id,
                                             @RequestBody PermissionDTO reqPermission) {

        reqPermission.setId(id);
        Set<ConstraintViolation<PermissionDTO>> violations = validator.validate(reqPermission);

        if (!violations.isEmpty()) {
            List<Map<String, String>> errorList = violations.stream().map(v -> {
                Map<String, String> err = new HashMap<>();
                err.put("field", v.getPropertyPath().toString());
                err.put("message", v.getMessage());
                return err;
            }).collect(Collectors.toList());
            throw new CustomValidationException(errorList);
        }

        Permission permission = this.permissionService.handleUpdatePermission(id, reqPermission);
        if (permission == null) {
            throw new NotFoundException("Không tìm thấy quyền");
        }

        return ResponseEntity.status(HttpStatus.OK).body(permission);
    }

    @DeleteMapping(path = "/secure/permissions/{id}")
    @ApiMessage(message = "Xóa quyền")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Integer id) {
        Permission permission = this.permissionService.getPermissionById(id);
        if (permission == null) {
            throw new NotFoundException("Không tìm thấy quyền");
        }

        try {
            this.permissionService.deletePermissionById(permission.getId());
        } catch (DataIntegrityViolationException ex) {
            throw new DataConflictException("Không thể xóa quyền này!");
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
