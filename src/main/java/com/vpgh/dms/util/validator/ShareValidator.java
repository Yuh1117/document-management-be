package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.annotation.ValidShare;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShareValidator implements ConstraintValidator<ValidShare, ShareReq> {
    private final FolderService folderService;
    private final DocumentService documentService;
    private final UserService userService;

    public ShareValidator(FolderService folderService, DocumentService documentService, UserService userService) {
        this.folderService = folderService;
        this.documentService = documentService;
        this.userService = userService;
    }

    @Override
    public boolean isValid(ShareReq shareReq, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        if (shareReq.getDocumentId() == null && shareReq.getFolderId() == null) {
            context.buildConstraintViolationWithTemplate("Tài liệu hoặc thư mục không được để trống.")
                    .addPropertyNode("id")
                    .addConstraintViolation();
            return false;
        }

        if (shareReq.getDocumentId() != null) {
            Document doc = this.documentService.getDocumentById(shareReq.getDocumentId());
            if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
                context.buildConstraintViolationWithTemplate("Tài liệu không tồn tại hoặc đã bị xóa")
                        .addPropertyNode("documentId")
                        .addConstraintViolation();
                valid = false;
            }
        }

        if (shareReq.getFolderId() != null) {
            Folder folder = this.folderService.getFolderById(shareReq.getDocumentId());
            if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
                context.buildConstraintViolationWithTemplate("Thư mục không tồn tại hoặc đã bị xóa")
                        .addPropertyNode("folderId")
                        .addConstraintViolation();
                valid = false;
            }
        }

        List<ShareReq.UserShareDTO> usersShare = shareReq.getShares();
        if (usersShare != null) {
            for (int i = 0; i < usersShare.size(); i++) {
                String email = usersShare.get(i).getEmail();
                if (email == null || email.isEmpty()) {
                    context.buildConstraintViolationWithTemplate("Email không được để trống.")
                            .addPropertyNode("member " + (i + 1))
                            .addConstraintViolation();
                    valid = false;
                } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    context.buildConstraintViolationWithTemplate("Email không hợp lệ.")
                            .addPropertyNode("member " + (i + 1))
                            .addConstraintViolation();
                    valid = false;
                } else {
                    User user = this.userService.getUserByEmail(usersShare.get(i).getEmail());
                    if (user == null) {
                        context.buildConstraintViolationWithTemplate("Không tìm thấy người dùng với email: " + usersShare.get(i).getEmail())
                                .addPropertyNode("email")
                                .addConstraintViolation();
                        valid = false;
                    }
                }
            }
        }


        return valid;
    }
}

