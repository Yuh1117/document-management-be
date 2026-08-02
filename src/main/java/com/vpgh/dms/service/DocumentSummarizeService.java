package com.vpgh.dms.service;

import java.util.UUID;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentSummary;

public interface DocumentSummarizeService {

    DocumentSummary summarizeDocument(Document doc, String language);

    DocumentSummary getLatestSummary(UUID documentId);

}
