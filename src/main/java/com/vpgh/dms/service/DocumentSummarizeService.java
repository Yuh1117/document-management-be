package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentSummary;

public interface DocumentSummarizeService {

    DocumentSummary summarizeDocument(Document doc, String language);

}
