package com.vpgh.dms.service;
import java.util.UUID;

public interface ProcessorIndexService {

    void deleteIndex(UUID docId);

    void renameIndex(UUID docId, String name);
}
