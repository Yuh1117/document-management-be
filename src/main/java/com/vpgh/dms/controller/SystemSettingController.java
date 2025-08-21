package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.SystemSettingDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.SystemSetting;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.CustomValidationException;
import com.vpgh.dms.util.exception.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
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
public class SystemSettingController {
    private final SystemSettingService systemSettingService;
    private final Validator validator;

    public SystemSettingController(SystemSettingService systemSettingService, Validator validator) {
        this.systemSettingService = systemSettingService;
        this.validator = validator;
    }

    @PostMapping(path = "/admin/settings")
    @ApiMessage(message = "Tạo mới cài đặt")
    public ResponseEntity<SystemSetting> create(@RequestBody @Valid SystemSettingDTO reqSetting) {
        SystemSetting setting = this.systemSettingService.handleCreateSetting(reqSetting);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.systemSettingService.save(setting));
    }

    @GetMapping(path = "/admin/settings")
    @ApiMessage(message = "Lấy danh sách cài đặt")
    public ResponseEntity<PaginationResDTO<List<SystemSetting>>> list(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        Page<SystemSetting> pageSettings = this.systemSettingService.getAllSettings(params);
        List<SystemSetting> roles = pageSettings.getContent();

        PaginationResDTO<List<SystemSetting>> results = new PaginationResDTO<>();
        results.setResult(roles);
        results.setCurrentPage(pageSettings.getNumber() + 1);
        results.setTotalPages(pageSettings.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping(path = "/admin/settings/{id}")
    @ApiMessage(message = "Lấy chi tiết cài đặt")
    public ResponseEntity<SystemSetting> detail(@PathVariable(value = "id") Integer id) {
        SystemSetting setting = this.systemSettingService.getSettingById(id);
        if (setting == null) {
            throw new NotFoundException("Không tìm thấy cài đặt");
        }

        return ResponseEntity.status(HttpStatus.OK).body(setting);
    }


    @PatchMapping(path = "/admin/settings/{id}")
    @ApiMessage(message = "Cập nhật cài đặt")
    public ResponseEntity<SystemSetting> update(@PathVariable("id") Integer id,
                                                @RequestBody SystemSettingDTO reqSetting) {

        SystemSetting setting = this.systemSettingService.getSettingById(id);
        if (setting == null) {
            throw new NotFoundException("Không tìm thấy cài đặt");
        }

        reqSetting.setId(setting.getId());
        Set<ConstraintViolation<SystemSettingDTO>> violations = validator.validate(reqSetting);

        if (!violations.isEmpty()) {
            List<Map<String, String>> errorList = violations.stream().map(v -> {
                Map<String, String> err = new HashMap<>();
                err.put("field", v.getPropertyPath().toString());
                err.put("message", v.getMessage());
                return err;
            }).collect(Collectors.toList());
            throw new CustomValidationException(errorList);
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.systemSettingService.handleUpdateSetting(setting, reqSetting));
    }

    @DeleteMapping(path = "/admin/settings/{id}")
    @ApiMessage(message = "Xóa cài đặt")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Integer id) {
        SystemSetting setting = this.systemSettingService.getSettingById(id);
        if (setting == null) {
            throw new NotFoundException("Không tìm thấy cài đặt");
        }

        this.systemSettingService.deleteSettingById(setting.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
