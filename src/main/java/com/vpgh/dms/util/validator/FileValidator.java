package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.request.FileUploadReq;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

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

        if (fileUploadReq == null || fileUploadReq.getFile() == null || fileUploadReq.getFile().isEmpty()) {
            context.buildConstraintViolationWithTemplate("File không được để trống!")
                    .addPropertyNode("file")
                    .addConstraintViolation();
            return false;
        }

        List<String> allowedTypes = List.of(this.systemSettingService.getSettingByKey("allowedFileType").getValue().split(";"));
        if (!allowedTypes.contains(fileUploadReq.getFile().getContentType())) {
            context.buildConstraintViolationWithTemplate("Loại file không hợp lệ!")
                    .addPropertyNode("file")
                    .addConstraintViolation();
            return false;
        }

        long maxSize = Long.parseLong(this.systemSettingService.getSettingByKey("maxFileSize").getValue());
        if (fileUploadReq.getFile().getSize() > maxSize) {
            context.buildConstraintViolationWithTemplate("Dung lượng file vượt quá giới hạn cho phép!")
                    .addPropertyNode("file")
                    .addConstraintViolation();
            return false;
        }

        if (fileUploadReq.getFolderId() != null) {
            Folder folder = this.folderService.getFolderById(fileUploadReq.getFolderId());
            if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
                context.buildConstraintViolationWithTemplate("Thư mục không tồn tại hoặc đã bị xóa")
                        .addPropertyNode("folderId")
                        .addConstraintViolation();
                return false;
            }
        }

        return valid;
    }
}
