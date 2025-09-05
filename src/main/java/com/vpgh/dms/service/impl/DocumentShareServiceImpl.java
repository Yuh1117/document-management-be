package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.*;
import com.vpgh.dms.repository.DocumentShareRepository;
import com.vpgh.dms.repository.FolderShareRepository;
import com.vpgh.dms.repository.UserRepository;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.DocumentShareService;
import com.vpgh.dms.service.EmailService;
import com.vpgh.dms.service.UserGroupService;
import com.vpgh.dms.util.SecurityUtil;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DocumentShareServiceImpl implements DocumentShareService {
    private final DocumentShareRepository documentShareRepository;
    private final UserGroupService userGroupService;
    private final UserRepository userRepository;
    private final DocumentService documentService;
    private final EmailService emailService;
    private final FolderShareRepository folderShareRepository;

    public DocumentShareServiceImpl(DocumentShareRepository documentShareRepository, UserGroupService userGroupService,
                                    UserRepository userRepository, DocumentService documentService, EmailService emailService,
                                    FolderShareRepository folderShareRepository) {
        this.documentShareRepository = documentShareRepository;
        this.userGroupService = userGroupService;
        this.userRepository = userRepository;
        this.documentService = documentService;
        this.emailService = emailService;
        this.folderShareRepository = folderShareRepository;
    }

    @Override
    public List<DocumentShare> saveAll(List<DocumentShare> documentShare) {
        return this.documentShareRepository.saveAll(documentShare);
    }

    @Override
    public boolean checkCanView(User user, Document doc) {
        return hasDocumentPermission(user, doc, ShareType.VIEW);
    }

    @Override
    public boolean checkCanEdit(User user, Document doc) {
        return hasDocumentPermission(user, doc, ShareType.EDIT);
    }

    @Override
    public boolean hasDocumentPermission(User user, Document doc, ShareType required) {
        if (this.documentService.isOwnerDocument(doc, user)) {
            return true;
        }

        if (required == ShareType.VIEW) {
            return checkUserOrGroupPermission(user, doc, ShareType.VIEW)
                    || checkUserOrGroupPermission(user, doc, ShareType.EDIT);
        } else {
            return checkUserOrGroupPermission(user, doc, ShareType.EDIT);
        }
    }

    @Override
    public List<DocumentShare> shareDocument(Document doc, List<ShareReq.UserShareDTO> userShareDTOS) throws MessagingException {
        List<DocumentShare> shares = new ArrayList<>();
        boolean isNew = false;
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        for (ShareReq.UserShareDTO dto : userShareDTOS) {
            isNew = false;
            User user = this.userRepository.findByEmail(dto.getEmail());
            if (this.documentService.isOwnerDocument(doc, user)) continue;

            DocumentShare existing = this.documentShareRepository.findByDocumentAndUser(doc, user).orElse(null);
            if (existing != null) {
                existing.setShareType(dto.getShareType());
                shares.add(existing);
            } else {
                DocumentShare share = new DocumentShare();
                share.setDocument(doc);
                share.setUser(user);
                share.setShareType(dto.getShareType());
                shares.add(share);
                isNew = true;
            }

            if (isNew) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("owner", currentUser.getEmail());
                variables.put("fileName", doc.getName());
                variables.put("type", "file");

                this.emailService.sendHtmlEmail(user.getEmail(), "Bạn được chia sẻ một tài liệu", variables);
            }
        }

        this.documentShareRepository.saveAll(shares);
        return shares;
    }

    @Override
    public List<DocumentShare> getShares(Document doc) {
        return this.documentShareRepository.findByDocument(doc);
    }

    @Override
    @Transactional
    public void removeShares(Document doc, List<User> users) {
        this.documentShareRepository.deleteByDocumentAndUserIn(doc, users);
    }

    @Override
    public List<DocumentShare> handleShareAfterUpload(Folder folder, Document document) {
        List<FolderShare> folderShares = this.folderShareRepository.findByFolder(folder);
        if (!folderShares.isEmpty()) {
            User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

            List<DocumentShare> ds = new ArrayList<>();
            for (FolderShare fs : folderShares) {
                if (!fs.getUser().getId().equals(currentUser.getId())) {
                    DocumentShare share = new DocumentShare();
                    share.setDocument(document);
                    share.setUser(fs.getUser());
                    share.setShareType(ShareType.VIEW);
                    ds.add(share);
                }
            }
            if (!folder.getCreatedBy().getId().equals(currentUser.getId())) {
                DocumentShare share = new DocumentShare();
                share.setDocument(document);
                share.setUser(folder.getCreatedBy());
                share.setShareType(ShareType.VIEW);
                ds.add(share);
            }
            return this.documentShareRepository.saveAll(ds);
        }
        return null;
    }

    private boolean checkUserOrGroupPermission(User user, Document doc, ShareType permission) {
        Optional<DocumentShare> userPermission = documentShareRepository
                .findByDocumentAndUserAndShareType(doc, user, permission);

        if (userPermission.isPresent()) {
            return true;
        }

        List<UserGroup> userGroups = this.userGroupService.getGroupsByUser(user);
        if (!userGroups.isEmpty()) {
            Optional<DocumentShare> groupPermission = documentShareRepository
                    .findByDocumentAndGroupInAndShareType(doc, userGroups, permission);

            if (groupPermission.isPresent()) {
                return true;
            }
        }

        return false;
    }

}
