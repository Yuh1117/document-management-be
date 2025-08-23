package com.vpgh.dms.service;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentShare;
import com.vpgh.dms.model.entity.User;

import java.util.List;

public interface DocumentShareService {
    boolean checkCanView(User user, Document doc);

    boolean checkCanEdit(User user, Document doc);

    boolean hasDocumentPermission(User user, Document doc, ShareType required);

    List<DocumentShare> shareDocument(Document doc, List<ShareReq.UserShareDTO> userShareDTOS);

    List<DocumentShare> getShares(Document doc);

    void removeShares(Document doc, List<User> users);
}
