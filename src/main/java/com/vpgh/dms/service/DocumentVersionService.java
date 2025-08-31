package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface DocumentVersionService {
    Page<DocumentVersion> getVersionsByDocument(Map<String, String> params, Document doc);
}
