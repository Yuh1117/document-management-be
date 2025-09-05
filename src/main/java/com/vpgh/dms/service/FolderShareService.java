package com.vpgh.dms.service;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.FolderShare;
import com.vpgh.dms.model.entity.User;
import jakarta.mail.MessagingException;

import java.util.List;

public interface FolderShareService {
    boolean checkCanView(User user, Folder folder);

    boolean checkCanEdit(User user, Folder folder);

    boolean hasFolderPermission(User user, Folder folder, ShareType required);

    List<FolderShare> shareFolder(Folder folder, List<ShareReq.UserShareDTO> userShareDTOS) throws MessagingException;

    List<FolderShare> getShares(Folder folder);

    void removeShares(Folder folder, List<User> users);

    List<FolderShare> handleShareAfterCreate(Folder parent, Folder folder);
}
