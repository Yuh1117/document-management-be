package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.request.FileUploadReq;
import com.vpgh.dms.util.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class FileValidator implements ConstraintValidator<ValidFile, FileUploadReq> {
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

        List<String> allowedTypes = List.of("application/pdf", "image/png", "image/jpeg");
        if (!allowedTypes.contains(fileUploadReq.getFile().getContentType())) {
            context.buildConstraintViolationWithTemplate("Loại file không hợp lệ!")
                    .addPropertyNode("file")
                    .addConstraintViolation();
            return false;
        }

        long maxSize = 10 * 1024 * 1024;
        if (fileUploadReq.getFile().getSize() > maxSize) {
            context.buildConstraintViolationWithTemplate("Dung lượng file vượt quá giới hạn cho phép!")
                    .addPropertyNode("file")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
