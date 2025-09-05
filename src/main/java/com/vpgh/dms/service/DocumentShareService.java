package com.vpgh.dms.service;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentShare;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import jakarta.mail.MessagingException;

import java.util.List;

public interface DocumentShareService {
    List<DocumentShare> saveAll(List<DocumentShare> documentShare);

    boolean checkCanView(User user, Document doc);

    boolean checkCanEdit(User user, Document doc);

    boolean hasDocumentPermission(User user, Document doc, ShareType required);

    List<DocumentShare> shareDocument(Document doc, List<ShareReq.UserShareDTO> userShareDTOS) throws MessagingException;

    List<DocumentShare> getShares(Document doc);

    void removeShares(Document doc, List<User> users);

    List<DocumentShare> handleShareAfterUpload(Folder folder, Document document);
}
