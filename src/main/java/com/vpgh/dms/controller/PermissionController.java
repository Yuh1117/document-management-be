package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.PermissionDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.CustomValidationException;
import com.vpgh.dms.util.exception.DataConflictException;
import com.vpgh.dms.util.exception.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PermissionController {
    private final PermissionService permissionService;
    private final Validator validator;

    public PermissionController(PermissionService permissionService, Validator validator) {
        this.permissionService = permissionService;
        this.validator = validator;
    }

    @PostMapping(path = "/admin/permissions")
    @ApiMessage(key = "api.permission.create", message = "Create permission")
    public ResponseEntity<Permission> create(@RequestBody @Valid PermissionDTO reqPermission) {
        Permission permission = this.permissionService.handleCreatePermission(reqPermission);
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @GetMapping(path = "/admin/permissions")
    @ApiMessage(key = "api.permission.list", message = "List permissions")
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

    @GetMapping(path = "/admin/permissions/{id}")
    @ApiMessage(key = "api.permission.detail", message = "Get permission details")
    public ResponseEntity<Permission> detail(@PathVariable(value = "id") Integer id) {
        Permission permission = this.permissionService.getPermissionById(id);
        if (permission == null) {
            throw new NotFoundException("error.permission.notFound");
        }

        return ResponseEntity.status(HttpStatus.OK).body(permission);
    }

    @PatchMapping(path = "/admin/permissions/{id}")
    @ApiMessage(key = "api.permission.update", message = "Update permission")
    public ResponseEntity<Permission> update(@PathVariable("id") Integer id,
                                             @RequestBody PermissionDTO reqPermission) {

        Permission permission = this.permissionService.getPermissionById(id);
        if (permission == null) {
            throw new NotFoundException("error.permission.notFound");
        }

        reqPermission.setId(permission.getId());
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

        return ResponseEntity.status(HttpStatus.OK).body(this.permissionService.handleUpdatePermission(permission, reqPermission));
    }

    @DeleteMapping(path = "/admin/permissions/{id}")
    @ApiMessage(key = "api.permission.delete", message = "Delete permission")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Integer id) {
        Permission permission = this.permissionService.getPermissionById(id);
        if (permission == null) {
            throw new NotFoundException("error.permission.notFound");
        }

        try {
            this.permissionService.deletePermissionById(permission.getId());
        } catch (DataIntegrityViolationException ex) {
            throw new DataConflictException("error.permission.cannotDelete");
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/secure/check-permissions")
    public ResponseEntity<Map<String, Boolean>> checkPermissions(@RequestBody List<Map<String, String>> requests) {
        Map<String, Boolean> resultMap = new HashMap<>();

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        if (currentUser == null || currentUser.getRole() == null) {
            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }

        Set<Permission> userPermissions = new HashSet<>(permissionService.getPermissionsByRole(currentUser.getRole()));

        for (Map<String, String> req : requests) {
            String key = req.get("apiPath") + "|" + req.get("method").toUpperCase();
            boolean matched = userPermissions.stream().anyMatch(p ->
                    p.getApiPath().equals(req.get("apiPath")) && p.getMethod().equalsIgnoreCase(req.get("method")));
            resultMap.put(key, matched);
        }

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }
}
