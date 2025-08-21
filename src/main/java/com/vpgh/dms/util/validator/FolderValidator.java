package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.request.FolderUploadReq;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.annotation.ValidFolder;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FolderValidator implements ConstraintValidator<ValidFolder, FolderUploadReq> {

    private final SystemSettingService systemSettingService;

    public FolderValidator(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @Override
    public boolean isValid(FolderUploadReq folderUploadReq, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        List<MultipartFile> files = folderUploadReq.getFiles();

        if (files != null && !files.isEmpty()) {
            List<String> allowedTypes = List.of(this.systemSettingService.getSettingByKey("allowedFileType").getValue().split(";"));
            long maxSize = Long.parseLong(this.systemSettingService.getSettingByKey("maxFileSize").getValue());

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);

                if (!allowedTypes.contains(file.getContentType())) {
                    context.buildConstraintViolationWithTemplate("Loại file không hợp lệ: " + file.getOriginalFilename())
                            .addPropertyNode("file " + (i + 1))
                            .addConstraintViolation();
                    valid = false;
                }

                if (file.getSize() > maxSize) {
                    context.buildConstraintViolationWithTemplate("Dung lượng vượt quá giới hạn: " + file.getOriginalFilename())
                            .addPropertyNode("file " + (i + 1))
                            .addConstraintViolation();
                    valid = false;
                }
            }
        }

        return valid;
    }
}