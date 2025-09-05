package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.*;
import com.vpgh.dms.repository.DocumentShareRepository;
import com.vpgh.dms.repository.FolderShareRepository;
import com.vpgh.dms.repository.UserRepository;
import com.vpgh.dms.service.*;
import com.vpgh.dms.util.SecurityUtil;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FolderShareServiceImpl implements FolderShareService {
    private final FolderShareRepository folderShareRepository;
    private final UserRepository userRepository;
    private final DocumentShareRepository documentShareRepository;
    private final UserGroupService userGroupService;
    private final FolderService folderService;
    private final DocumentService documentService;
    private final EmailService emailService;


    public FolderShareServiceImpl(FolderShareRepository folderShareRepository,
                                  UserRepository userRepository,
                                  DocumentShareRepository documentShareRepository,
                                  UserGroupService userGroupService,
                                  FolderService folderService,
                                  DocumentService documentService,
                                  EmailService emailService) {
        this.folderShareRepository = folderShareRepository;
        this.userRepository = userRepository;
        this.documentShareRepository = documentShareRepository;
        this.userGroupService = userGroupService;
        this.folderService = folderService;
        this.documentService = documentService;
        this.emailService = emailService;
    }

    @Override
    public boolean checkCanView(User user, Folder folder) {
        return hasFolderPermission(user, folder, ShareType.VIEW);
    }

    @Override
    public boolean checkCanEdit(User user, Folder folder) {
        return hasFolderPermission(user, folder, ShareType.EDIT);
    }

    @Override
    public boolean hasFolderPermission(User user, Folder folder, ShareType required) {
        if (this.folderService.isOwnerFolder(folder, user)) {
            return true;
        }

        if (required == ShareType.VIEW) {
            return checkUserOrGroupPermission(user, folder, ShareType.VIEW)
                    || checkUserOrGroupPermission(user, folder, ShareType.EDIT);
        } else {
            return checkUserOrGroupPermission(user, folder, ShareType.EDIT);
        }
    }

    @Override
    public List<FolderShare> shareFolder(Folder folder, List<ShareReq.UserShareDTO> userShareDTOS) throws MessagingException {
        List<FolderShare> folderShares = new ArrayList<>();
        List<DocumentShare> documentShares = new ArrayList<>();

        if (folder.getInheritPermissions()) {
            List<Folder> allFolders = this.folderService.getAllDescendantsIncludingSelf(folder);
            List<Document> allDocuments = this.documentService.getAllDocumentsInFolders(allFolders);
            boolean isNew = false;
            User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

            for (ShareReq.UserShareDTO dto : userShareDTOS) {
                isNew = false;
                User user = this.userRepository.findByEmail(dto.getEmail());
                if (this.folderService.isOwnerFolder(folder, user)) continue;

                for (int i = 0; i < allFolders.size(); i++) {
                    if (this.folderService.isOwnerFolder(allFolders.get(i), user)) continue;

                    FolderShare existing = this.folderShareRepository.findByFolderAndUser(allFolders.get(i), user).orElse(null);
                    if (existing != null) {
                        existing.setShareType(dto.getShareType());
                        folderShares.add(existing);
                    } else {
                        FolderShare share = new FolderShare();
                        share.setFolder(allFolders.get(i));
                        share.setUser(user);
                        share.setShareType(dto.getShareType());
                        folderShares.add(share);
                        if (i == 0) {
                            isNew = true;
                        }
                    }
                }

                for (Document d : allDocuments) {
                    if (this.documentService.isOwnerDocument(d, user)) continue;

                    DocumentShare existing = this.documentShareRepository.findByDocumentAndUser(d, user).orElse(null);
                    if (existing != null) {
                        existing.setShareType(dto.getShareType());
                        documentShares.add(existing);
                    } else {
                        DocumentShare share = new DocumentShare();
                        share.setDocument(d);
                        share.setUser(user);
                        share.setShareType(dto.getShareType());
                        documentShares.add(share);
                    }
                }

                if (isNew) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("owner", currentUser.getEmail());
                    variables.put("fileName", allFolders.getFirst().getName());
                    variables.put("type", "folder");

                    this.emailService.sendHtmlEmail(user.getEmail(), "Bạn được chia sẻ một thư mục", variables);
                }

            }
            this.folderShareRepository.saveAll(folderShares);
            this.documentShareRepository.saveAll(documentShares);
        }

        return folderShares;
    }

    @Override
    public List<FolderShare> getShares(Folder folder) {
        return folderShareRepository.findByFolder(folder);
    }

    @Override
    @Transactional
    public void removeShares(Folder folder, List<User> users) {
        List<Folder> allFolders = folderService.getAllDescendantsIncludingSelf(folder);
        List<Document> allDocuments = documentService.getAllDocumentsInFolders(allFolders);

        this.folderShareRepository.deleteByFolderInAndUserIn(allFolders, users);
        this.documentShareRepository.deleteByDocumentInAndUserIn(allDocuments, users);
    }

    @Override
    public List<FolderShare> handleShareAfterCreate(Folder parent, Folder folder) {
        List<FolderShare> folderShares = this.getShares(parent);

        if (!folderShares.isEmpty()) {
            List<FolderShare> fShares = new ArrayList<>();
            List<DocumentShare> dShares = new ArrayList<>();

            if (folder.getInheritPermissions()) {
                List<Folder> allFolders = this.folderService.getAllDescendantsIncludingSelf(folder);
                List<Document> allDocuments = this.documentService.getAllDocumentsInFolders(allFolders);
                User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

                for (FolderShare fs : folderShares) {
                    if (!fs.getUser().getId().equals(currentUser.getId())) {
                        for (Folder f : allFolders) {
                            FolderShare share = new FolderShare();
                            share.setFolder(f);
                            share.setUser(fs.getUser());
                            share.setShareType(ShareType.VIEW);
                            fShares.add(share);
                        }

                        for (Document d : allDocuments) {
                            DocumentShare share = new DocumentShare();
                            share.setDocument(d);
                            share.setUser(fs.getUser());
                            share.setShareType(ShareType.VIEW);
                            dShares.add(share);
                        }
                    }
                }
                if (!parent.getCreatedBy().getId().equals(currentUser.getId())) {
                    for (Folder f : allFolders) {
                        FolderShare share = new FolderShare();
                        share.setFolder(f);
                        share.setUser(parent.getCreatedBy());
                        share.setShareType(ShareType.VIEW);
                        fShares.add(share);
                    }

                    for (Document d : allDocuments) {
                        DocumentShare share = new DocumentShare();
                        share.setDocument(d);
                        share.setUser(parent.getCreatedBy());
                        share.setShareType(ShareType.VIEW);
                        dShares.add(share);
                    }
                }

                this.folderShareRepository.saveAll(fShares);
                this.documentShareRepository.saveAll(dShares);
            }

            return fShares;
        }

        return null;
    }

    private boolean checkUserOrGroupPermission(User user, Folder folder, ShareType permission) {
        Optional<FolderShare> userPermission = this.folderShareRepository
                .findByFolderAndUserAndShareType(folder, user, permission);

        if (userPermission.isPresent()) {
            return true;
        }

        List<UserGroup> userGroups = this.userGroupService.getGroupsByUser(user);
        if (!userGroups.isEmpty()) {
            Optional<FolderShare> groupPermission = this.folderShareRepository
                    .findByFolderAndGroupInAndShareType(folder, userGroups, permission);

            if (groupPermission.isPresent()) {
                return true;
            }
        }

        return false;
    }
}
