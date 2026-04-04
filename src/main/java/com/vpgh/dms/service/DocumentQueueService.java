package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Document;

public interface DocumentQueueService {

    void publishDocument(Document doc);
}
