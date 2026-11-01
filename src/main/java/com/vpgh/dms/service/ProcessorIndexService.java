package com.vpgh.dms.service;
import com.vpgh.dms.model.entity.Document;

import java.util.UUID;

public interface ProcessorIndexService {

    void deleteIndex(UUID docId);

    void renameIndex(UUID docId, String name);

    void copyIndex(UUID sourceDocId, Document copy);
}
