package com.vpgh.dms.service;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.User;

public interface DocumentShareService {
    boolean checkCanView(User user, Document doc);

    boolean checkCanEdit(User user, Document doc);

    boolean checkUserOrGroupPermission(User user, Document doc, ShareType permission);
}
