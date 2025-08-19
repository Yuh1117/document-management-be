package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.request.FileUploadReq;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator implements ConstraintValidator<ValidFile, FileUploadReq> {

    @Autowired
    private SystemSettingService systemSettingService;
    @Autowired
    private FolderService folderService;

    @Override
    public boolean isValid(FileUploadReq fileUploadReq, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        List<MultipartFile> files = fileUploadReq.getFiles();
        if (files == null || files.isEmpty()) {
            context.buildConstraintViolationWithTemplate("Phải chọn ít nhất một file để upload.")
                    .addPropertyNode("files")
                    .addConstraintViolation();
            return false;
        }

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

        return valid;
    }
}
