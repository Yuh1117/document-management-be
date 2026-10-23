package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.request.FileUploadReq;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator implements ConstraintValidator<ValidFile, FileUploadReq> {

    private final SystemSettingService systemSettingService;
    private final MessageSource messageSource;

    public FileValidator(SystemSettingService systemSettingService, MessageSource messageSource) {
        this.systemSettingService = systemSettingService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(FileUploadReq fileUploadReq, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        List<MultipartFile> files = fileUploadReq.getFiles();
        if (files == null || files.isEmpty()) {
            ValidationMessages.reject(context, messageSource, "files", "validation.file.atLeastOne");
            return false;
        } else {
            List<String> allowedTypes = List.of(this.systemSettingService.getSettingByKey("allowedFileType").getValue().split(";"));
            long maxSize = Long.parseLong(this.systemSettingService.getSettingByKey("maxFileSize").getValue());

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);

                if (!allowedTypes.contains(file.getContentType())) {
                    ValidationMessages.reject(context, messageSource, "file " + (i + 1),
                            "validation.file.type.invalid", file.getOriginalFilename());
                    valid = false;
                }

                if (file.getSize() > maxSize) {
                    ValidationMessages.reject(context, messageSource, "file " + (i + 1),
                            "validation.file.size.exceeded", file.getOriginalFilename());
                    valid = false;
                }
            }
        }

        return valid;
    }
}
