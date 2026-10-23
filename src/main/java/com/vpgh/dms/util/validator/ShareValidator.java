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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShareValidator implements ConstraintValidator<ValidShare, ShareReq> {
    private final FolderService folderService;
    private final DocumentService documentService;
    private final UserService userService;
    private final MessageSource messageSource;

    public ShareValidator(FolderService folderService, DocumentService documentService, UserService userService,
                          MessageSource messageSource) {
        this.folderService = folderService;
        this.documentService = documentService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(ShareReq shareReq, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        if (shareReq.getDocumentId() == null && shareReq.getFolderId() == null) {
            ValidationMessages.reject(context, messageSource, "id", "validation.share.target.required");
            return false;
        }

        if (shareReq.getDocumentId() != null) {
            Document doc = this.documentService.getDocumentById(shareReq.getDocumentId());
            if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
                ValidationMessages.reject(context, messageSource, "documentId", "error.document.notFoundOrDeleted");
                valid = false;
            }
        }

        if (shareReq.getFolderId() != null) {
            Folder folder = this.folderService.getFolderById(shareReq.getFolderId());
            if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
                ValidationMessages.reject(context, messageSource, "folderId", "error.folder.notFoundOrDeleted");
                valid = false;
            }
        }

        List<ShareReq.UserShareDTO> usersShare = shareReq.getShares();
        if (usersShare != null) {
            for (int i = 0; i < usersShare.size(); i++) {
                String email = usersShare.get(i).getEmail();
                if (email == null || email.isEmpty()) {
                    ValidationMessages.reject(context, messageSource, "member " + (i + 1), "validation.email.notBlank");
                    valid = false;
                } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    ValidationMessages.reject(context, messageSource, "member " + (i + 1), "validation.email.invalid");
                    valid = false;
                } else {
                    User user = this.userService.getUserByEmail(usersShare.get(i).getEmail());
                    if (user == null) {
                        ValidationMessages.reject(context, messageSource, "email", "validation.email.userNotFound",
                                usersShare.get(i).getEmail());
                        valid = false;
                    }
                }
            }
        }


        return valid;
    }
}

