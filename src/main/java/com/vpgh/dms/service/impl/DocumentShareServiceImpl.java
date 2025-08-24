package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.repository.DocumentShareRepository;
import com.vpgh.dms.repository.UserRepository;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.DocumentShareService;
import com.vpgh.dms.service.FolderShareService;
import com.vpgh.dms.service.UserGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentShareServiceImpl implements DocumentShareService {
    private final DocumentShareRepository documentShareRepository;
    private final UserGroupService userGroupService;
    private final UserRepository userRepository;
    private final DocumentService documentService;

    public DocumentShareServiceImpl(DocumentShareRepository documentShareRepository, UserGroupService userGroupService,
                                    UserRepository userRepository, DocumentService documentService) {
        this.documentShareRepository = documentShareRepository;
        this.userGroupService = userGroupService;
        this.userRepository = userRepository;
        this.documentService = documentService;
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
    public List<DocumentShare> shareDocument(Document doc, List<ShareReq.UserShareDTO> userShareDTOS) {
        List<DocumentShare> shares = new ArrayList<>();

        for (ShareReq.UserShareDTO dto : userShareDTOS) {
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
